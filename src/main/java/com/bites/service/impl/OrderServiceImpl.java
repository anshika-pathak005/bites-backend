package com.bites.service.impl;

import com.bites.dto.request.CheckoutRequest;
import com.bites.dto.request.OrderFilterRequest;
import com.bites.dto.response.OrderItemResponse;
import com.bites.dto.response.OrderResponse;
import com.bites.dto.response.PageResponse;
import com.bites.entity.Cart;
import com.bites.entity.CartItem;
import com.bites.entity.Customer;
import com.bites.entity.DeliveryArea;
import com.bites.entity.MenuItem;
import com.bites.entity.Order;
import com.bites.entity.OrderItem;
import com.bites.entity.Payment;
import com.bites.entity.enums.OrderStatus;
import com.bites.entity.enums.PaymentMethod;
import com.bites.entity.enums.PaymentStatus;
import com.bites.exception.BadRequestException;
import com.bites.exception.DeliveryAreaNotServiceableException;
import com.bites.exception.ResourceNotFoundException;
import com.bites.repository.CartItemRepository;
import com.bites.repository.CartRepository;
import com.bites.repository.DeliveryAreaRepository;
import com.bites.repository.MenuItemRepository;
import com.bites.repository.OrderRepository;
import com.bites.repository.PaymentRepository;
import com.bites.repository.specification.OrderSpecifications;
import com.bites.service.CartService;
import com.bites.service.CustomerService;
import com.bites.service.OrderService;
import com.bites.util.OrderNumberGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {

    // Used to save the new Order row, and to look orders up again for
    // tracking, admin search, and status updates.
    private final OrderRepository orderRepository;

    // Used to save the Payment row that always gets created alongside an order.
    private final PaymentRepository paymentRepository;

    // Used only for the Cart Flow — to read what's currently sitting in the
    // guest's cart at the moment they check out.
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    // Used to clear the cart after a successful Cart Flow checkout, and to
    // re-use its existing "find or create" logic rather than duplicating it here.
    private final CartService cartService;

    // Used for the Buy Now Flow (loading a single item directly), and for
    // double-checking availability on every item right before the order is placed.
    private final MenuItemRepository menuItemRepository;

    // Used to confirm the chosen delivery area actually exists and is active,
    // and to read its delivery charge.
    private final DeliveryAreaRepository deliveryAreaRepository;

    // Used to find-or-create the Customer row for this checkout, keyed by phone.
    private final CustomerService customerService;

    // Generates the human-readable order id shown to the customer (e.g. "ORD-7K4QX9P2").
    private final OrderNumberGenerator orderNumberGenerator;

    // Describes exactly which status a given order status is allowed to move
    // to next. This mirrors the state machine documented in the project
    // plan, and stops an admin (or a buggy frontend) from doing something
    // like jumping straight from PENDING to DELIVERED.
    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED_STATUS_TRANSITIONS = Map.of(
            OrderStatus.PENDING, Set.of(OrderStatus.ACCEPTED, OrderStatus.CANCELLED),
            OrderStatus.ACCEPTED, Set.of(OrderStatus.PREPARING, OrderStatus.CANCELLED),
            OrderStatus.PREPARING, Set.of(OrderStatus.OUT_FOR_DELIVERY),
            OrderStatus.OUT_FOR_DELIVERY, Set.of(OrderStatus.DELIVERED),
            OrderStatus.DELIVERED, Set.of(),
            OrderStatus.CANCELLED, Set.of()
    );

    @Override
    @Transactional
    public OrderResponse checkout(CheckoutRequest request) {
        // Step 1:
        // Work out exactly which items are being ordered. This also handles
        // telling the Cart Flow and Buy Now Flow apart, and rejects the
        // request if it's ambiguous (both or neither were provided).
        List<LineItem> lineItems = resolveLineItems(request);

        // Step 2:
        // Confirm the chosen delivery area actually exists and is currently
        // serviceable — never place an order for an area we don't deliver to.
        DeliveryArea deliveryArea = deliveryAreaRepository.findById(request.deliveryAreaId())
                .orElseThrow(() -> new DeliveryAreaNotServiceableException(
                        "Invalid delivery area id: " + request.deliveryAreaId()));

        if (!deliveryArea.isActive()) {
            throw new DeliveryAreaNotServiceableException(
                    "We currently do not deliver to this area: " + deliveryArea.getName());
        }

        // Step 3:
        // Find this customer's existing record by phone, or create a new
        // one if this is their first time ordering — this is the "guest
        // upsert by phone" behaviour agreed on earlier.
        Customer customer = customerService.findOrCreateByPhone(request.name(), request.phone());

        // Step 4:
        // Work out the money: each line item's subtotal, the order's overall
        // subtotal, and the final grand total including delivery charge.
        BigDecimal subtotal = lineItems.stream()
                .map(LineItem::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal deliveryCharge = deliveryArea.getDeliveryCharge();
        BigDecimal grandTotal = subtotal.add(deliveryCharge);

        // Step 5:
        // Build the Order entity itself. customerName/customerPhone/
        // deliveryAddress/landmark/deliveryCharge are all SNAPSHOTS taken
        // right now — even if the customer's saved details change later,
        // this order will always show exactly what it showed at checkout time.
        Order order = Order.builder()
                .orderNumber(generateUniqueOrderNumber())
                .customer(customer)
                .customerName(request.name())
                .customerPhone(request.phone())
                .deliveryAddress(request.fullAddress())
                .landmark(request.landmark())
                .deliveryArea(deliveryArea)
                .deliveryCharge(deliveryCharge)
                .subtotal(subtotal)
                .grandTotal(grandTotal)
                .status(OrderStatus.PENDING)
                .build();

        // Step 6:
        // Attach one OrderItem per line item. Like the order itself, each
        // OrderItem snapshots the item's name and price at this exact moment.
        for (LineItem lineItem : lineItems) {
            OrderItem orderItem = OrderItem.builder()
                    .menuItem(lineItem.menuItem())
                    .itemName(lineItem.menuItem().getName())
                    .itemPrice(lineItem.menuItem().getPrice())
                    .quantity(lineItem.quantity())
                    .subtotal(lineItem.subtotal())
                    .build();
            order.addItem(orderItem);
        }

        // Step 7:
        // Save the order. Because OrderItem is cascaded from Order, saving
        // the order also saves every attached OrderItem in the same operation.
        Order savedOrder = orderRepository.save(order);

        // Step 8:
        // Create the payment record. v1 only ever creates a COD/PENDING row
        // — this becomes PAID once the delivery person actually collects cash,
        // which is handled outside this checkout step (e.g. when status
        // moves to DELIVERED).
        Payment payment = Payment.builder()
                .order(savedOrder)
                .method(PaymentMethod.COD)
                .status(PaymentStatus.PENDING)
                .build();
        paymentRepository.save(payment);

        // Step 9:
        // If this was a Cart Flow checkout, the cart has now been "used up"
        // — empty it so the guest doesn't see stale items still sitting
        // there after their order is placed.
        if (request.cartSessionId() != null) {
            cartService.clearCart(request.cartSessionId());
        }

        // Step 10:
        // Convert the freshly created order into the response shape and send it back.
        return toResponse(savedOrder);
    }

    @Override
    public OrderResponse trackByOrderNumber(String orderNumber) {
        // Step 1:
        // Look the order up by its human-readable order number (not the
        // internal database id — the customer never sees that).
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("No order found with id: " + orderNumber));

        // Step 2:
        // Convert Entity -> Response DTO.
        return toResponse(order);
    }

    @Override
    public List<OrderResponse> trackByPhone(String phone) {
        // Step 1:
        // Find every order ever placed with this phone number, most recent first.
        // Step 2:
        // Convert each one into a response DTO. An empty list here is a
        // perfectly normal result (this phone just hasn't ordered before),
        // not an error.
        return orderRepository.findByCustomerPhoneOrderByPlacedAtDesc(phone).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public PageResponse<OrderResponse> searchForAdmin(OrderFilterRequest filter, Pageable pageable) {
        // Step 1:
        // Run the admin's chosen filters (status/search/date range) against
        // the orders table, one page at a time.
        Page<Order> page = orderRepository.findAll(OrderSpecifications.fromFilter(filter), pageable);

        // Step 2:
        // Convert every order on this page into a response DTO, then wrap
        // the whole page (items + paging info) into one PageResponse.
        return PageResponse.from(page.map(this::toResponse));
    }

    @Override
    public OrderResponse getByIdForAdmin(Long id) {
        // Step 1:
        // Fetch the order by its internal database id (admin screens link
        // using this id, unlike customer tracking which uses the order number).
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id));

        // Step 2:
        // Convert Entity -> Response DTO.
        return toResponse(order);
    }

    @Override
    @Transactional
    public OrderResponse updateStatus(Long id, OrderStatus newStatus) {
        // Step 1:
        // Load the order we're about to update.
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id));

        // Step 2:
        // Check the requested status change is actually allowed from where
        // this order currently is — e.g. you cannot skip straight from
        // PENDING to DELIVERED, and you cannot change a DELIVERED order at all.
        Set<OrderStatus> allowedNextStatuses = ALLOWED_STATUS_TRANSITIONS.get(order.getStatus());
        if (!allowedNextStatuses.contains(newStatus)) {
            throw new BadRequestException(
                    "Cannot change order status from " + order.getStatus() + " to " + newStatus);
        }

        // Step 3:
        // The change is valid — apply it and save.
        order.setStatus(newStatus);
        Order saved = orderRepository.save(order);

        // Step 4:
        // Convert Entity -> Response DTO.
        return toResponse(saved);
    }

    @Override
    public OrderResponse toResponse(Order order) {
        // Step 1:
        // Convert every order item on this order into its response shape.
        List<OrderItemResponse> itemResponses = order.getOrderItems().stream()
                .map(item -> new OrderItemResponse(
                        item.getId(),
                        item.getMenuItem().getId(),
                        item.getItemName(),
                        item.getItemPrice(),
                        item.getQuantity(),
                        item.getSubtotal()
                ))
                .toList();

        // Step 2:
        // Load this order's payment row so we can show its method/status —
        // Order itself doesn't hold a direct reference to Payment (Payment
        // points to Order, not the other way round), so this needs its own lookup.
        Payment payment = paymentRepository.findByOrderId(order.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payment record missing for order: " + order.getOrderNumber()));

        // Step 3:
        // Package everything — order fields, items, delivery area name, and
        // payment info — into the final response object.
        return new OrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getStatus(),
                itemResponses,
                order.getCustomerName(),
                order.getCustomerPhone(),
                order.getDeliveryAddress(),
                order.getLandmark(),
                order.getDeliveryArea().getName(),
                order.getDeliveryCharge(),
                order.getSubtotal(),
                order.getGrandTotal(),
                payment.getMethod(),
                payment.getStatus(),
                order.getPlacedAt()
        );
    }

    // Internal helper: works out exactly what is being ordered, for either
    // the Cart Flow or the Buy Now Flow, and makes sure the request isn't
    // ambiguous (both or neither flow selected).
    private List<LineItem> resolveLineItems(CheckoutRequest request) {
        boolean hasCartFlow = request.cartSessionId() != null && !request.cartSessionId().isBlank();
        boolean hasBuyNowFlow = request.buyNowMenuItemId() != null;

        if (hasCartFlow == hasBuyNowFlow) {
            // Either BOTH were provided or NEITHER was — both are invalid states.
            throw new BadRequestException(
                    "Provide exactly one of cartSessionId (Cart Flow) or buyNowMenuItemId (Buy Now Flow)");
        }

        if (hasCartFlow) {
            return resolveCartFlowLineItems(request.cartSessionId());
        }
        return resolveBuyNowLineItems(request.buyNowMenuItemId(), request.buyNowQuantity());
    }

    // Internal helper: reads the guest's current cart and turns every cart
    // item into a checkout line item, re-checking availability along the way
    // in case something went out of stock after it was added to the cart.
    private List<LineItem> resolveCartFlowLineItems(String cartSessionId) {
        Cart cart = cartRepository.findBySessionId(cartSessionId)
                .orElseThrow(() -> new BadRequestException("No cart found for this session"));

        List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getId());
        if (cartItems.isEmpty()) {
            throw new BadRequestException("Your cart is empty");
        }

        List<LineItem> lineItems = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            MenuItem menuItem = cartItem.getMenuItem();
            assertAvailable(menuItem);
            lineItems.add(new LineItem(menuItem, cartItem.getQuantity()));
        }
        return lineItems;
    }

    // Internal helper: loads the single item for a Buy Now checkout and
    // confirms it's actually available right now.
    private List<LineItem> resolveBuyNowLineItems(Long menuItemId, Integer requestedQuantity) {
        MenuItem menuItem = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found: " + menuItemId));
        assertAvailable(menuItem);

        // Default to 1 if the frontend didn't send a quantity at all.
        int quantity = requestedQuantity != null ? requestedQuantity : 1;
        return List.of(new LineItem(menuItem, quantity));
    }

    private void assertAvailable(MenuItem menuItem) {
        if (!menuItem.isAvailable()) {
            throw new BadRequestException("This item is currently out of stock: " + menuItem.getName());
        }
    }

    // Internal helper: keeps generating a random order number until it finds
    // one that isn't already used. Collisions are astronomically unlikely
    // given the random alphabet + length used, but we guard against it anyway.
    private String generateUniqueOrderNumber() {
        String candidate;
        do {
            candidate = orderNumberGenerator.generate();
        } while (orderRepository.findByOrderNumber(candidate).isPresent());
        return candidate;
    }

    // Small internal-only holder pairing a menu item with the quantity being
    // ordered, plus its calculated line subtotal — used only while building
    // an order, never exposed outside this class.
    private record LineItem(MenuItem menuItem, int quantity) {
        BigDecimal subtotal() {
            return menuItem.getPrice().multiply(BigDecimal.valueOf(quantity));
        }
    }
}

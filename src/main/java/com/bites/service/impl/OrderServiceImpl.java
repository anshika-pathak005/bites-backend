package com.bites.service.impl;

import com.bites.dto.request.CheckoutRequest;
import com.bites.dto.request.OrderFilterRequest;
import com.bites.dto.response.OrderItemResponse;
import com.bites.dto.response.OrderResponse;
import com.bites.dto.response.PageResponse;
import com.bites.entity.Cart;
import com.bites.entity.CartItem;
import com.bites.entity.Customer;
import com.bites.entity.MenuItem;
import com.bites.entity.Order;
import com.bites.entity.OrderItem;
import com.bites.entity.Payment;
import com.bites.entity.Restaurant;
import com.bites.entity.enums.OrderStatus;
import com.bites.entity.enums.PaymentMethod;
import com.bites.entity.enums.PaymentStatus;
import com.bites.exception.BadRequestException;
import com.bites.exception.ResourceNotFoundException;
import com.bites.repository.CartItemRepository;
import com.bites.repository.CartRepository;
import com.bites.repository.CustomerRepository;
import com.bites.repository.MenuItemRepository;
import com.bites.repository.OrderRepository;
import com.bites.repository.PaymentRepository;
import com.bites.repository.RestaurantRepository;
import com.bites.repository.specification.OrderSpecifications;
import com.bites.service.CartService;
import com.bites.service.CustomerService;
import com.bites.service.DeliveryPricingResult;
import com.bites.service.DeliveryPricingService;
import com.bites.service.OrderService;
import com.bites.util.OrderNumberGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final CartService cartService;
    private final MenuItemRepository menuItemRepository;
    private final DeliveryPricingService deliveryPricingService;
    private final RestaurantRepository restaurantRepository;
    private final CustomerService customerService;
    private final CustomerRepository customerRepository;
    private final OrderNumberGenerator orderNumberGenerator;

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
    public OrderResponse checkout(Long customerId, String guestId, CheckoutRequest request) {
        log.info("Creating order");

        try {
            List<LineItem> lineItems = resolveLineItems(customerId, guestId, request);

            DeliveryPricingResult pricing = deliveryPricingService.priceDelivery(request.latitude(), request.longitude());

            // Logged-in caller: link the order straight to their own
            // account instead of looking one up/creating one by phone —
            // avoids ever creating a second, disconnected Customer row for
            // someone who already has a real account.
            Customer customer = customerId != null
                    ? customerRepository.findById(customerId)
                        .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + customerId))
                    : customerService.findOrCreateByPhone(request.name(), request.phone());

            BigDecimal subtotal = lineItems.stream()
                    .map(LineItem::subtotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal grandTotal = subtotal.add(pricing.charge());

            Order order = Order.builder()
                    .orderNumber(generateUniqueOrderNumber())
                    .customer(customer)
                    .guestId(guestId)
                    .customerName(request.name())
                    .customerPhone(request.phone())
                    .deliveryAddress(request.fullAddress())
                    .landmark(request.landmark())
                    .pincode(request.pincode())
                    .distanceKm(BigDecimal.valueOf(pricing.distanceKm()))
                    .deliveryCharge(pricing.charge())
                    .subtotal(subtotal)
                    .grandTotal(grandTotal)
                    .status(OrderStatus.PENDING)
                    .build();

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

            Order savedOrder = orderRepository.save(order);

            Payment payment = Payment.builder()
                    .order(savedOrder)
                    .method(PaymentMethod.COD)
                    .status(PaymentStatus.PENDING)
                    .build();
            paymentRepository.save(payment);

            if (request.buyNowMenuItemId() == null) {
                cartService.clearCart(customerId, guestId);
            }

            log.info("Order created: orderNumber={}", savedOrder.getOrderNumber());
            return toResponse(savedOrder);
        } catch (Exception e) {
            log.error("Failed to create order", e);
            throw e;
        }
    }

    @Override
    public List<OrderResponse> listMine(Long customerId, String guestId) {
        List<Order> orders = customerId != null
                ? orderRepository.findByCustomer_IdOrderByPlacedAtDesc(customerId)
                : orderRepository.findByGuestIdOrderByPlacedAtDesc(guestId);
        return orders.stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public OrderResponse trackByOrderNumber(String orderNumber) {
        log.debug("Tracking order: orderNumber={}", orderNumber);
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("No order found with id: " + orderNumber));
        return toResponse(order);
    }

    @Override
    @Transactional
    public OrderResponse cancel(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("No order found with id: " + orderNumber));

        if (order.getStatus() != OrderStatus.PENDING) {
            log.warn("Order cancellation rejected: order is already {}", order.getStatus());
            throw new BadRequestException(
                    "This order can no longer be cancelled — it is already " + order.getStatus());
        }

        int windowMinutes = restaurantRepository.findAll().stream().findFirst()
                .map(Restaurant::getCancellationWindowMinutes)
                .orElse(5);
        Duration sincePlaced = Duration.between(order.getPlacedAt(), LocalDateTime.now());
        if (sincePlaced.toMinutes() >= windowMinutes) {
            log.warn("Order cancellation rejected: cancellation window expired");
            throw new BadRequestException(
                    "The " + windowMinutes + "-minute cancellation window for this order has passed");
        }

        order.setStatus(OrderStatus.CANCELLED);
        Order saved = orderRepository.save(order);
        log.info("Order cancelled: orderNumber={}", orderNumber);
        return toResponse(saved);
    }

    @Override
    public PageResponse<OrderResponse> searchForAdmin(OrderFilterRequest filter, Pageable pageable) {
        Page<Order> page = orderRepository.findAll(OrderSpecifications.fromFilter(filter), pageable);
        return PageResponse.from(page.map(this::toResponse));
    }

    @Override
    public OrderResponse getByIdForAdmin(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id));
        return toResponse(order);
    }

    @Override
    @Transactional
    public OrderResponse updateStatus(Long id, OrderStatus newStatus) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id));

        Set<OrderStatus> allowedNextStatuses = ALLOWED_STATUS_TRANSITIONS.get(order.getStatus());
        if (!allowedNextStatuses.contains(newStatus)) {
            log.warn("Order status update rejected: cannot change from {} to {}", order.getStatus(), newStatus);
            throw new BadRequestException(
                    "Cannot change order status from " + order.getStatus() + " to " + newStatus);
        }

        OrderStatus oldStatus = order.getStatus();
        order.setStatus(newStatus);
        Order saved = orderRepository.save(order);
        log.info("Order status updated: orderNumber={}, oldStatus={}, newStatus={}", order.getOrderNumber(), oldStatus, newStatus);
        return toResponse(saved);
    }

    @Override
    public OrderResponse toResponse(Order order) {
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

        Payment payment = paymentRepository.findByOrderId(order.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payment record missing for order: " + order.getOrderNumber()));

        boolean cancellable = isCancellable(order);

        return new OrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getStatus(),
                itemResponses,
                order.getCustomerName(),
                order.getCustomerPhone(),
                order.getDeliveryAddress(),
                order.getLandmark(),
                order.getPincode(),
                order.getDistanceKm(),
                order.getDeliveryCharge(),
                order.getSubtotal(),
                order.getGrandTotal(),
                payment.getMethod(),
                payment.getStatus(),
                order.getPlacedAt(),
                cancellable
        );
    }

    private boolean isCancellable(Order order) {
        if (order.getStatus() != OrderStatus.PENDING) {
            return false;
        }
        int windowMinutes = restaurantRepository.findAll().stream().findFirst()
                .map(Restaurant::getCancellationWindowMinutes)
                .orElse(5);
        Duration sincePlaced = Duration.between(order.getPlacedAt(), LocalDateTime.now());
        return sincePlaced.toMinutes() < windowMinutes;
    }

    private List<LineItem> resolveLineItems(Long customerId, String guestId, CheckoutRequest request) {
        if (request.buyNowMenuItemId() != null) {
            return resolveBuyNowLineItems(request.buyNowMenuItemId(), request.buyNowQuantity());
        }
        return resolveCartFlowLineItems(customerId, guestId);
    }

    private List<LineItem> resolveCartFlowLineItems(Long customerId, String guestId) {
        Cart cart = (customerId != null ? cartRepository.findByCustomerId(customerId) : cartRepository.findByGuestId(guestId))
                .orElseThrow(() -> new BadRequestException("No cart found for this guest"));

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

    private List<LineItem> resolveBuyNowLineItems(Long menuItemId, Integer requestedQuantity) {
        MenuItem menuItem = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found: " + menuItemId));
        assertAvailable(menuItem);

        int quantity = requestedQuantity != null ? requestedQuantity : 1;
        return List.of(new LineItem(menuItem, quantity));
    }

    private void assertAvailable(MenuItem menuItem) {
        if (!menuItem.isAvailable()) {
            throw new BadRequestException("This item is currently out of stock: " + menuItem.getName());
        }
    }

    private String generateUniqueOrderNumber() {
        String candidate;
        do {
            candidate = orderNumberGenerator.generate();
        } while (orderRepository.findByOrderNumber(candidate).isPresent());
        return candidate;
    }

    private record LineItem(MenuItem menuItem, int quantity) {
        BigDecimal subtotal() {
            return menuItem.getPrice().multiply(BigDecimal.valueOf(quantity));
        }
    }
}

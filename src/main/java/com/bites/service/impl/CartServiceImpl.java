package com.bites.service.impl;

import com.bites.dto.response.CartItemResponse;
import com.bites.dto.response.CartResponse;
import com.bites.entity.Cart;
import com.bites.entity.CartItem;
import com.bites.entity.MenuItem;
import com.bites.exception.BadRequestException;
import com.bites.exception.ResourceNotFoundException;
import com.bites.repository.CartItemRepository;
import com.bites.repository.CartRepository;
import com.bites.repository.MenuItemRepository;
import com.bites.service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartServiceImpl implements CartService {

    // Used to load and save the Cart row itself (one row per guest OR per logged-in customer).
    private final CartRepository cartRepository;

    // Used to load and save the individual line items inside a cart.
    private final CartItemRepository cartItemRepository;

    // Used to check a menu item actually exists (and is available) before
    // it is allowed to be added to a cart.
    private final MenuItemRepository menuItemRepository;

    @Override
    public CartResponse getOrCreateCart(Long customerId, String guestId) {
        Cart cart = getOrCreateCartEntity(customerId, guestId);
        return buildCartResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse addItem(Long customerId, String guestId, Long menuItemId, int quantity) {
        // Step 1:
        // Make sure this caller has a cart to add into (creates one if needed).
        Cart cart = getOrCreateCartEntity(customerId, guestId);

        // Step 2:
        // Load the menu item being added and confirm it actually exists and
        // is currently available — we should never let someone add an
        // out-of-stock or deleted item to their cart.
        MenuItem menuItem = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found: " + menuItemId));

        if (!menuItem.isAvailable()) {
            log.warn("Cannot add out-of-stock item to cart: menuItemId={}, name={}", menuItemId, menuItem.getName());
            throw new BadRequestException("This item is currently out of stock: " + menuItem.getName());
        }

        // Step 3:
        // Check whether this exact item is already sitting in the cart.
        var existingItem = cartItemRepository.findByCartIdAndMenuItemId(cart.getId(), menuItemId);

        if (existingItem.isPresent()) {
            // Step 4a:
            // Item is already in the cart — just increase how many they want,
            // instead of creating a second, duplicate row for the same dish.
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
            cartItemRepository.save(item);
        } else {
            // Step 4b:
            // Item is new to this cart — create a fresh row for it.
            // priceAtAddition freezes today's price onto this cart row, so a
            // later menu price change won't silently change what's in the cart.
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .menuItem(menuItem)
                    .quantity(quantity)
                    .priceAtAddition(menuItem.getPrice())
                    .build();
            cartItemRepository.save(newItem);
        }

        log.info("Item added to cart: menuItemId={}, quantity={}", menuItemId, quantity);
        // Step 5:
        // Return the cart as it looks now, with the new/updated item included.
        return buildCartResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse updateItemQuantity(Long customerId, String guestId, Long cartItemId, int quantity) {
        // Step 1:
        // Find the caller's cart. If there isn't one, there's nothing to update.
        Cart cart = findCartOrThrow(customerId, guestId);

        // Step 2:
        // Find the specific cart item and confirm it actually belongs to
        // THIS cart — without this check, one caller could edit another
        // caller's cart item just by guessing an id.
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found: " + cartItemId));

        if (!item.getCart().getId().equals(cart.getId())) {
            log.warn("Mismatched cart item access attempt: cartItemId={}", cartItemId);
            throw new BadRequestException("This cart item does not belong to the given cart");
        }

        // Step 3:
        // A quantity of 0 means "remove this item" — otherwise just update
        // the stored quantity.
        if (quantity == 0) {
            cartItemRepository.delete(item);
            log.info("Item removed from cart: cartItemId={}", cartItemId);
        } else {
            item.setQuantity(quantity);
            cartItemRepository.save(item);
            log.info("Cart item quantity updated: cartItemId={}, newQuantity={}", cartItemId, quantity);
        }

        // Step 4:
        // Return the cart as it looks now, after the change.
        return buildCartResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse removeItem(Long customerId, String guestId, Long cartItemId) {
        // Removing an item is really just "update quantity to 0" — reuse
        // that same logic instead of duplicating it.
        return updateItemQuantity(customerId, guestId, cartItemId, 0);
    }

    @Override
    @Transactional
    public void clearCart(Long customerId, String guestId) {
        // Step 1:
        // Find the cart for this caller — if there isn't one, there is
        // nothing to clear, so we simply do nothing instead of throwing.
        findCart(customerId, guestId).ifPresent(cart -> {
            // Step 2:
            // Delete every item that belongs to this cart (used right
            // after an order is successfully placed).
            cartItemRepository.deleteByCartId(cart.getId());
            log.info("Cart cleared for cartId: {}", cart.getId());
        });
    }

    // Internal helper: looks up the caller's cart WITHOUT creating one —
    // customerId wins over guestId whenever both are available.
    private java.util.Optional<Cart> findCart(Long customerId, String guestId) {
        return customerId != null
                ? cartRepository.findByCustomerId(customerId)
                : cartRepository.findByGuestId(guestId);
    }

    // Internal helper: same lookup as findCart, but fails clearly instead
    // of returning empty — used by methods that only make sense against an
    // already-existing cart (updating/removing an item).
    private Cart findCartOrThrow(Long customerId, String guestId) {
        return findCart(customerId, guestId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        customerId != null ? "Cart not found for this customer" : "Cart not found for this guest"));
    }

    // Internal helper: same lookup-or-create logic as getOrCreateCart, but
    // returns the raw Cart entity instead of a response DTO, since other
    // methods in this class need the entity itself to attach items to it.
    private Cart getOrCreateCartEntity(Long customerId, String guestId) {
        if (customerId != null) {
            return cartRepository.findByCustomerId(customerId)
                    .orElseGet(() -> cartRepository.save(Cart.builder().customerId(customerId).build()));
        }
        return cartRepository.findByGuestId(guestId)
                .orElseGet(() -> cartRepository.save(Cart.builder().guestId(guestId).build()));
    }

    // Internal helper: turns a Cart entity into the full CartResponse the
    // frontend expects, including every item and the calculated grand subtotal.
    private CartResponse buildCartResponse(Cart cart) {
        // Step 1:
        // Load every item row that belongs to this cart.
        List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getId());

        // Step 2:
        // Convert each raw CartItem entity into a CartItemResponse, working
        // out that item's line subtotal (unit price x quantity) along the way.
        List<CartItemResponse> itemResponses = cartItems.stream()
                .map(item -> new CartItemResponse(
                        item.getId(),
                        item.getMenuItem().getId(),
                        item.getMenuItem().getName(),
                        item.getMenuItem().getImageUrl(),
                        item.getPriceAtAddition(),
                        item.getQuantity(),
                        item.getPriceAtAddition().multiply(BigDecimal.valueOf(item.getQuantity()))
                ))
                .toList();

        // Step 3:
        // Add up every item's subtotal to get the cart's overall subtotal.
        BigDecimal cartSubtotal = itemResponses.stream()
                .map(CartItemResponse::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Step 4:
        // Package everything into the final response object.
        return new CartResponse(cart.getId(), itemResponses, cartSubtotal);
    }
}

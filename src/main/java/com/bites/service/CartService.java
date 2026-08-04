package com.bites.service;

import com.bites.dto.response.CartResponse;

// Handles everything related to a guest's shopping cart, before checkout.
public interface CartService {

    // Returns the cart for this session. If the guest doesn't have a cart
    // row yet (first-time visitor), a new empty cart is created for them.
    CartResponse getOrCreateCart(String sessionId);

    // Adds a menu item to the cart. If the same item is already in the cart,
    // this increases its quantity instead of creating a duplicate row.
    CartResponse addItem(String sessionId, Long menuItemId, int quantity);

    // Changes the quantity of one existing cart item. Setting quantity to 0
    // removes the item completely.
    CartResponse updateItemQuantity(String sessionId, Long cartItemId, int quantity);

    // Removes one item from the cart entirely.
    CartResponse removeItem(String sessionId, Long cartItemId);

    // Empties the cart completely. Used after an order is successfully placed.
    void clearCart(String sessionId);
}

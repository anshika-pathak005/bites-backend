package com.bites.service;

import com.bites.dto.response.CartResponse;

// Handles everything related to a guest's shopping cart, before checkout.
// Every method is keyed by guestId — resolved by the controller from the
// HttpOnly guestId cookie (see GuestIdentityFilter), never client-supplied.
public interface CartService {

    // Returns the cart for this guest. If they don't have a cart row yet
    // (first-time visitor), a new empty cart is created for them.
    CartResponse getOrCreateCart(String guestId);

    // Adds a menu item to the cart. If the same item is already in the cart,
    // this increases its quantity instead of creating a duplicate row.
    CartResponse addItem(String guestId, Long menuItemId, int quantity);

    // Changes the quantity of one existing cart item. Setting quantity to 0
    // removes the item completely.
    CartResponse updateItemQuantity(String guestId, Long cartItemId, int quantity);

    // Removes one item from the cart entirely.
    CartResponse removeItem(String guestId, Long cartItemId);

    // Empties the cart completely. Used after an order is successfully placed.
    void clearCart(String guestId);
}

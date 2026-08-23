package com.bites.service;

import com.bites.dto.response.CartResponse;

// Handles everything related to a shopping cart, before checkout. Every
// method takes BOTH customerId and guestId — exactly one of them applies
// per call, and customerId always wins when present:
//   - customerId != null: caller is logged in, cart is keyed by customerId.
//   - customerId == null: caller is a guest, cart is keyed by guestId
//     (resolved by the controller from the HttpOnly guestId cookie, see
//     GuestIdentityFilter), exactly as this worked before login existed.
// This lets a guest keep using the cart exactly as before, while a logged-in
// customer's cart follows them across devices/browsers instead.
public interface CartService {

    // Returns the cart for this caller. If they don't have a cart row yet
    // (first-time visitor, or first-time logged-in customer), a new empty
    // cart is created for them.
    CartResponse getOrCreateCart(Long customerId, String guestId);

    // Adds a menu item to the cart. If the same item is already in the cart,
    // this increases its quantity instead of creating a duplicate row.
    CartResponse addItem(Long customerId, String guestId, Long menuItemId, int quantity);

    // Changes the quantity of one existing cart item. Setting quantity to 0
    // removes the item completely.
    CartResponse updateItemQuantity(Long customerId, String guestId, Long cartItemId, int quantity);

    // Removes one item from the cart entirely.
    CartResponse removeItem(Long customerId, String guestId, Long cartItemId);

    // Empties the cart completely. Used after an order is successfully placed.
    void clearCart(Long customerId, String guestId);
}

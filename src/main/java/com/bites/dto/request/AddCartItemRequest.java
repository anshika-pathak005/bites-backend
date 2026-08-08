package com.bites.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

// Sent by the frontend when a guest clicks "Add to Cart" on a menu item.
// Deliberately does NOT carry any guest/session identifier — the backend
// resolves whose cart this is from the HttpOnly guestId cookie (see
// GuestIdentityFilter), never from something the client could tamper with.
public record AddCartItemRequest(

        // Which menu item is being added.
        @NotNull(message = "menuItemId is required")
        Long menuItemId,

        // How many of this item to add. Must be at least 1 — you cannot add
        // "0 fries" to a cart.
        @Min(value = 1, message = "quantity must be at least 1")
        int quantity
) {
}

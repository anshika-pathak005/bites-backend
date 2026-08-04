package com.bites.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// Sent by the frontend when a guest clicks "Add to Cart" on a menu item.
public record AddCartItemRequest(

        // Which guest's cart this belongs to. The frontend generates this once
        // per browser and sends it with every cart request.
        @NotBlank(message = "sessionId is required")
        String sessionId,

        // Which menu item is being added.
        @NotNull(message = "menuItemId is required")
        Long menuItemId,

        // How many of this item to add. Must be at least 1 — you cannot add
        // "0 fries" to a cart.
        @Min(value = 1, message = "quantity must be at least 1")
        int quantity
) {
}

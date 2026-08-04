package com.bites.dto.request;

import jakarta.validation.constraints.Min;

// Sent when the guest changes the quantity of something already in their cart
// (the +/- buttons next to a cart line item).
public record UpdateCartItemRequest(

        // New quantity for this cart item. If this is 0, the service will
        // remove the item entirely instead of saving a 0-quantity row.
        @Min(value = 0, message = "quantity cannot be negative")
        int quantity
) {
}

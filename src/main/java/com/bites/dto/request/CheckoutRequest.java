package com.bites.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

// Sent when the guest clicks "Place Order" — covers BOTH the Cart Flow and
// the Buy Now Flow, since they end up needing the same delivery details and
// only differ in WHAT is being ordered.
public record CheckoutRequest(

        // --- Delivery details — same for both flows ---

        @NotBlank(message = "Name is required")
        String name,

        @NotBlank(message = "Phone number is required")
        String phone,

        @NotNull(message = "Delivery area is required")
        Long deliveryAreaId,

        @NotBlank(message = "Delivery address is required")
        String fullAddress,

        // Optional — helps the delivery person find the exact spot.
        String landmark,

        // --- Exactly ONE of the two sections below must be filled in ---
        // The service layer checks this manually (a plain annotation can't
        // easily express "one of these two, not both, not neither").

        // Cart Flow: set this to check out everything currently in the
        // guest's cart. Leave null for a Buy Now order.
        String cartSessionId,

        // Buy Now Flow: set this + buyNowQuantity to check out a single item
        // directly from its product page, skipping the cart entirely.
        // Leave null for a Cart Flow order.
        Long buyNowMenuItemId,

        @Positive(message = "Quantity must be at least 1")
        Integer buyNowQuantity
) {
}

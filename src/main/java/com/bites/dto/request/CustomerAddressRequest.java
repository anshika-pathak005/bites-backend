package com.bites.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// Sent when a customer (or the checkout flow) saves a new delivery address.
public record CustomerAddressRequest(

        // Optional friendly name for this address, e.g. "Home" or "Work".
        String label,

        @NotBlank(message = "Full address is required")
        String fullAddress,

        // Optional — a landmark to help the delivery person find the place.
        String landmark,

        // Which delivery area this address belongs to. Required because we
        // must know the delivery charge and confirm we actually deliver there.
        @NotNull(message = "deliveryAreaId is required")
        Long deliveryAreaId,

        // If true, this becomes the address shown by default at checkout.
        boolean isDefault
) {
}

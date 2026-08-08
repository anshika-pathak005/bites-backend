package com.bites.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

// Sent from the admin "Delivery Pricing" screen to create or update a
// distance slab (e.g. "0-2km -> ₹20").
public record DeliveryChargeSlabRequest(

        @NotNull(message = "minDistanceKm is required")
        @DecimalMin(value = "0.0", message = "minDistanceKm cannot be negative")
        BigDecimal minDistanceKm,

        @NotNull(message = "maxDistanceKm is required")
        @DecimalMin(value = "0.0", message = "maxDistanceKm cannot be negative")
        BigDecimal maxDistanceKm,

        @NotNull(message = "Delivery charge is required")
        @DecimalMin(value = "0.0", message = "Delivery charge cannot be negative")
        BigDecimal charge,

        boolean isActive
) {
}

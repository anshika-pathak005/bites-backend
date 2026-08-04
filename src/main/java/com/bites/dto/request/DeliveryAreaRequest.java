package com.bites.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

// Sent from the admin "Delivery Areas" screen to create or update a delivery area.
public record DeliveryAreaRequest(

        @NotBlank(message = "Name is required")
        @Size(max = 150, message = "Name must be at most 150 characters")
        String name,

        @NotNull(message = "Delivery charge is required")
        @DecimalMin(value = "0.0", message = "Delivery charge cannot be negative")
        BigDecimal deliveryCharge,

        boolean isActive
) {
}

package com.bites.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalTime;

// Sent from the admin "Restaurant Settings" screen. There is only ever one
// restaurant row, so this always updates that single row — there is no
// "create" for this one.
public record RestaurantSettingsRequest(

        @NotBlank(message = "Name is required")
        String name,

        String description,

        String logoUrl,

        String email,

        String phone,

        String address,

        @NotNull(message = "Opening time is required")
        LocalTime openingTime,

        @NotNull(message = "Closing time is required")
        LocalTime closingTime,

        // Manual override — lets the admin mark the restaurant as closed
        // even during normal opening hours (e.g. fully booked, staff shortage).
        boolean isOpen,

        @NotNull(message = "Minimum order amount is required")
        @DecimalMin(value = "0.0", message = "Minimum order amount cannot be negative")
        BigDecimal minOrderAmount
) {
}

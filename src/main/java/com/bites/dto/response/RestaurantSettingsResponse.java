package com.bites.dto.response;

import java.math.BigDecimal;
import java.time.LocalTime;

// Restaurant-wide settings shown on the customer "About/Info" screen and
// edited in the admin "Settings" screen.
public record RestaurantSettingsResponse(
        Long id,
        String name,
        String description,
        String logoUrl,
        String email,
        String phone,
        String address,
        LocalTime openingTime,
        LocalTime closingTime,

        // Manual override so the admin can mark the restaurant closed even
        // during normal opening hours, e.g. for a holiday.
        boolean isOpen,

        // Smallest order total a customer must reach before they can checkout.
        BigDecimal minOrderAmount
) {
}

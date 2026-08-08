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

        // Restaurant's own coordinates, used as the origin point for the
        // 8km delivery-radius check on guest addresses. Null until an admin
        // sets them on the Settings screen.
        BigDecimal latitude,
        BigDecimal longitude,

        // Minutes after placedAt a guest can still self-cancel a PENDING order.
        int cancellationWindowMinutes,

        LocalTime openingTime,
        LocalTime closingTime,

        // Manual override so the admin can mark the restaurant closed even
        // during normal opening hours, e.g. for a holiday.
        boolean isOpen,

        // Smallest order total a customer must reach before they can checkout.
        BigDecimal minOrderAmount
) {
}

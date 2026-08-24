package com.bites.dto.request;

import jakarta.validation.constraints.DecimalMax;
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

        // Restaurant's own coordinates — the fixed point every guest
        // address's delivery distance (Haversine, maxDeliveryRadiusKm
        // below) is measured from. Optional so existing settings can still
        // be saved before an admin gets round to pinning the exact
        // location; radius validation simply stays disabled until both are
        // set.
        @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
        @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
        BigDecimal latitude,

        @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
        @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
        BigDecimal longitude,

        // Minutes after an order is placed that a guest can still cancel it
        // themselves (only while it's still PENDING). e.g. 5.
        @jakarta.validation.constraints.Min(value = 1, message = "Cancellation window must be at least 1 minute")
        int cancellationWindowMinutes,

        // Max straight-line distance (km) a delivery address can be from
        // the restaurant — was a hardcoded 8km constant, now editable here.
        // Should stay >= the highest active DeliveryChargeSlab's max, or
        // checkout will reject addresses no slab can price (see
        // DeliveryPricingServiceImpl.resolveCharge).
        @NotNull(message = "Max delivery radius is required")
        @DecimalMin(value = "0.1", message = "Max delivery radius must be greater than 0")
        BigDecimal maxDeliveryRadiusKm,

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

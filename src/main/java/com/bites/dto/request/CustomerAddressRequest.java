package com.bites.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

// Sent when a customer (or the checkout flow) saves a new delivery address,
// or edits an existing one (see AddressService.addAddress/updateAddress).
// Deliberately does NOT include a delivery area/zone id — delivery
// serviceability and pricing are both derived purely from latitude/longitude
// on the backend (see DeliveryPricingService), so the frontend never needs
// to know about zones at all.
public record CustomerAddressRequest(

        // Optional friendly name for this address, e.g. "Home" or "Work".
        String label,

        @NotBlank(message = "Full address is required")
        String fullAddress,

        // Optional — a landmark to help the delivery person find the place.
        String landmark,

        // Optional — not every area has one handy, and it's not needed for
        // the distance check (that uses lat/lng), so it isn't required.
        String pincode,

        // Required — either taken straight from the browser's Geolocation
        // API (current-location flow) or produced by geocoding the typed
        // address on the frontend (manual-entry flow). This is the ONLY
        // thing the backend uses to validate the 8km delivery radius.
        @NotNull(message = "Latitude is required")
        @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
        @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
        BigDecimal latitude,

        @NotNull(message = "Longitude is required")
        @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
        @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
        BigDecimal longitude,

        // If true, this becomes the address shown by default at checkout.
        boolean isDefault
) {
}

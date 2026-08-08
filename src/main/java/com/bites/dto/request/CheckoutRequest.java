package com.bites.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

// Sent when the guest clicks "Place Order" — covers BOTH the Cart Flow and
// the Buy Now Flow, since they end up needing the same delivery details and
// only differ in WHAT is being ordered.
//
// Deliberately does NOT include a delivery area/zone id (see
// DeliveryPricingService) or a cart/session id: which cart to check out is
// implicit — it's always the current guestId's cart (resolved from the
// HttpOnly cookie, see GuestIdentityFilter), never something the client
// specifies. name/phone below are purely delivery CONTACT info for whoever
// delivers the food — they are never used to look up or authorize
// anything; ownership of the cart/order is guestId, not the phone number.
public record CheckoutRequest(

        // --- Delivery details — same for both flows ---

        @NotBlank(message = "Name is required")
        String name,

        @NotBlank(message = "Phone number is required")
        String phone,

        @NotBlank(message = "Delivery address is required")
        String fullAddress,

        // Optional — helps the delivery person find the exact spot.
        String landmark,

        // Optional — not required for pricing/validation (that uses lat/lng).
        String pincode,

        // Required — the backend validates the 8km delivery radius and
        // resolves the delivery charge from these coordinates. Should be
        // whatever the guest's chosen/saved address already has (see
        // CustomerAddressResponse), or freshly geocoded if they typed a
        // brand new address at checkout.
        @NotNull(message = "Latitude is required")
        @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
        @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
        BigDecimal latitude,

        @NotNull(message = "Longitude is required")
        @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
        @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
        BigDecimal longitude,

        // --- Which flow this is ---
        // Leave both of these null/absent for a Cart Flow order (checks out
        // whatever is currently in this guest's cart). Set BOTH to do a
        // Buy Now order instead (skips the cart, orders this one item directly).

        Long buyNowMenuItemId,

        @Positive(message = "Quantity must be at least 1")
        Integer buyNowQuantity
) {
}

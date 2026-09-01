package com.bites.dto.response;

import java.math.BigDecimal;
import java.util.List;

// Wraps the admin "Delivery Pricing" screen's slab list together with the
// restaurant's current max delivery radius (Restaurant.maxDeliveryRadiusKm)
// — so the frontend can show "Max Coverage: 8km" next to the slab table
// without a second API call.
public record DeliveryChargeSlabsResponse(
        List<DeliveryChargeSlabResponse> slabs,
        BigDecimal maxDeliveryRadiusKm
) {
}

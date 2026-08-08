package com.bites.dto.response;

import java.math.BigDecimal;

// One distance -> flat fee slab, e.g. "0-2km -> ₹20". Shown on the admin
// "Delivery Pricing" screen; also implicitly what the customer-facing
// checkout flow is priced against (see DeliveryPricingService).
public record DeliveryChargeSlabResponse(
        Long id,
        BigDecimal minDistanceKm,
        BigDecimal maxDistanceKm,
        BigDecimal charge,
        boolean isActive
) {
}

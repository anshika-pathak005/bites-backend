package com.bites.dto.response;

import java.math.BigDecimal;

// Sent back when the frontend asks "what would delivery cost for this
// address?" — e.g. on the checkout page, before the guest actually places
// the order. Same numbers checkout itself will use (see
// DeliveryPricingService), just exposed as a preview.
public record DeliveryQuoteResponse(

        // Straight-line distance (km) from the restaurant to this point.
        double distanceKm,

        // The delivery charge for that distance, resolved from the
        // configured DeliveryChargeSlab rows.
        BigDecimal deliveryCharge
) {
}

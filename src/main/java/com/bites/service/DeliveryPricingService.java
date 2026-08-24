package com.bites.service;

import java.math.BigDecimal;

// All delivery-distance logic lives here so it's calculated exactly once,
// on the backend, and reused everywhere a point needs to be validated/priced
// — currently AddressServiceImpl (saving/editing an address) and
// OrderServiceImpl (checkout). The frontend never computes or sends a
// distance or a delivery charge; it only ever sends latitude/longitude.
public interface DeliveryPricingService {

    // Throws AddressOutOfDeliveryRangeException if the given point is
    // further than the restaurant's max delivery radius (admin-configurable). Used when a
    // customer just saves/edits an address, where only serviceability
    // matters, not the price.
    double validateWithinDeliveryRadius(BigDecimal latitude, BigDecimal longitude);

    // Same radius validation as above, but also resolves the delivery
    // charge for that distance via the configured slabs. Used at checkout,
    // where the actual price is needed.
    DeliveryPricingResult priceDelivery(BigDecimal latitude, BigDecimal longitude);
}

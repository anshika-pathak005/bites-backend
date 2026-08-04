package com.bites.dto.response;

import java.math.BigDecimal;

// A deliverable area/zone (e.g. a neighborhood) with its own delivery fee —
// used at checkout to work out the delivery charge, and managed in the
// admin "Delivery Areas" screen.
public record DeliveryAreaResponse(
        Long id,
        String name,

        // Flat delivery fee charged for orders going to this area.
        BigDecimal deliveryCharge,
        boolean isActive
) {
}

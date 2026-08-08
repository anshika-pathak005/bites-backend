package com.bites.service;

import java.math.BigDecimal;

// Result of pricing one delivery point: how far it is from the restaurant,
// and what it should be charged based on the configured distance slabs.
public record DeliveryPricingResult(double distanceKm, BigDecimal charge) {
}

package com.bites.exception;

/**
 * Thrown when a customer address's lat/lng is further than the restaurant's configured max radius
 * delivery radius from the restaurant (see DeliveryPricingServiceImpl).
 * Surfaced to the client as a clear, actionable 400 — distinct from
 * DeliveryAreaNotServiceableException so the frontend can show a specific
 * "too far away" message.
 */
public class AddressOutOfDeliveryRangeException extends RuntimeException {
    public AddressOutOfDeliveryRangeException(String message) {
        super(message);
    }
}

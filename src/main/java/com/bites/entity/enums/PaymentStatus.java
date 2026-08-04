package com.bites.entity.enums;

// Tracks the state of a payment for an order, from being taken through to
// completion or refund.
public enum PaymentStatus {
    PENDING,
    PAID,
    FAILED,
    REFUNDED
}

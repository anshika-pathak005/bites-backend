package com.bites.entity.enums;

/**
 * Mirrors the CHECK constraint on orders.status and the state machine
 * documented in the project plan:
 * PENDING -> ACCEPTED -> PREPARING -> OUT_FOR_DELIVERY -> DELIVERED
 * PENDING/ACCEPTED -> CANCELLED
 */
public enum OrderStatus {
    PENDING,
    ACCEPTED,
    PREPARING,
    OUT_FOR_DELIVERY,
    DELIVERED,
    CANCELLED
}

package com.bites.entity.enums;

// The payment methods a customer can use for an order. Only COD (cash on
// delivery) is used today; the others are reserved for a future payment
// gateway integration (see Payment.java).
public enum PaymentMethod {
    COD,
    CARD,
    UPI,
    WALLET
}

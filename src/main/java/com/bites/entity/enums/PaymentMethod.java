package com.bites.entity.enums;

// The payment methods a customer can use for an order.
// COD (cash on delivery) — the original v1 default.
// ONLINE — paid upfront via Razorpay (card/UPI/wallet/netbanking are all
// collapsed into this one value; Razorpay itself decides which instrument
// the customer actually used, we don't need that level of detail in our DB).
// CARD/UPI/WALLET are kept for backward compatibility but unused —
// superseded by ONLINE.
public enum PaymentMethod {
    COD,
    CARD,
    UPI,
    WALLET,
    ONLINE
}

package com.bites.dto.response;

import com.bites.entity.enums.OrderStatus;
import com.bites.entity.enums.PaymentMethod;
import com.bites.entity.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

// The full order, sent to the frontend — used by order tracking (customer
// side) and the order detail view (admin side). Every "customer.../
// "delivery..." field here is a SNAPSHOT taken at checkout time, so this
// stays accurate even if the customer's live address changes later.
public record OrderResponse(

        Long id,

        // Human-readable id shown to the customer, e.g. "ORD-7K4QX9P2".
        String orderNumber,

        // Where this order currently sits in the Pending -> ... -> Delivered flow.
        OrderStatus status,

        List<OrderItemResponse> items,

        String customerName,

        String customerPhone,

        String deliveryAddress,

        String landmark,

        String pincode,

        // Straight-line distance (km) from the restaurant, used to price
        // this order — see DeliveryPricingService.
        BigDecimal distanceKm,

        BigDecimal deliveryCharge,

        // Sum of all item subtotals, before delivery charge.
        BigDecimal subtotal,

        // subtotal + deliveryCharge — the actual amount to collect on delivery.
        BigDecimal grandTotal,

        // Always "COD" in v1, but modeled as a real field so a payment
        // gateway can be added later without changing this response shape.
        PaymentMethod paymentMethod,

        PaymentStatus paymentStatus,

        // Only populated right after checkout with paymentMethod=ONLINE,
        // while the payment is still PENDING — this is what the frontend
        // needs to open the Razorpay Checkout widget. Null for COD orders
        // and null again once the payment has already been verified/paid
        // (see OrderServiceImpl.toResponse).
        String razorpayOrderId,

        // Razorpay's public key id — safe to expose, the Checkout widget
        // requires it. Same nullability as razorpayOrderId above.
        String razorpayKeyId,

        LocalDateTime placedAt,

        // True if THIS order can be self-cancelled right now (status is
        // still PENDING and placedAt is within the restaurant's configured
        // cancellation window) — computed on the backend so the frontend
        // never has to duplicate that time-window logic; it just shows/hides
        // the Cancel button based on this flag. The backend re-checks both
        // conditions again on the actual cancel call regardless.
        boolean cancellable
) {
}

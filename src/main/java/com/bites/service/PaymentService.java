package com.bites.service;

import com.bites.dto.request.PaymentVerificationRequest;
import com.bites.entity.Order;
import com.bites.entity.Payment;

// Business logic for online (Razorpay) payments. COD payments never touch
// this — see OrderServiceImpl.checkout, which only calls here when
// CheckoutRequest.paymentMethod() is ONLINE.
public interface PaymentService {

    // Called right after an ONLINE order is saved. Creates the Razorpay
    // order for this order's server-computed grandTotal (never a
    // client-supplied amount — see PaymentServiceImpl) and saves a Payment
    // row (method=ONLINE, status=PENDING) with the resulting Razorpay order
    // id, so the frontend can open the Checkout widget next.
    Payment createOnlinePayment(Order order);

    // Called by the frontend right after the Razorpay Checkout widget's
    // success callback fires. Re-verifies the payment itself (HMAC
    // signature, then a fetch-and-compare against Razorpay's own records)
    // rather than trusting the frontend — only marks the Payment PAID if
    // both checks pass. Returns the updated Payment.
    Payment verifyAndCapture(PaymentVerificationRequest request);

    // Called by Razorpay's servers directly (not the frontend) when a
    // payment's status changes — the reliability safety net in case the
    // frontend's verify call never happens (browser closed, network drop,
    // etc). Verifies the webhook signature before trusting the payload, and
    // updates the matching Payment idempotently.
    void handleWebhook(String rawPayload, String signatureHeader);
}

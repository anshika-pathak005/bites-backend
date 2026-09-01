package com.bites.dto.request;

import jakarta.validation.constraints.NotBlank;

// Sent by the frontend immediately after the Razorpay Checkout widget's
// success callback fires. These 3 values are exactly what Razorpay hands
// back to the frontend on a successful payment — the backend re-verifies
// them itself (HMAC signature + a fetch-and-compare against Razorpay's own
// records) rather than trusting the frontend's word that payment succeeded.
public record PaymentVerificationRequest(

        @NotBlank(message = "razorpayOrderId is required")
        String razorpayOrderId,

        @NotBlank(message = "razorpayPaymentId is required")
        String razorpayPaymentId,

        @NotBlank(message = "razorpaySignature is required")
        String razorpaySignature
) {
}

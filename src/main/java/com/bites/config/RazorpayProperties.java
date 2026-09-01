package com.bites.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/*
 * Holds the Razorpay credentials read from application.yml (backed by
 * RAZORPAY_KEY_ID / RAZORPAY_KEY_SECRET / RAZORPAY_WEBHOOK_SECRET in .env —
 * see .env.example). No values are hardcoded or defaulted anywhere in code;
 * if any of these are missing, the app fails to start rather than silently
 * running against the wrong (or no) Razorpay account.
 */
@Component
public class RazorpayProperties {

    // Public identifier for the Razorpay account — safe to hand back to the
    // frontend (see OrderResponse.razorpayKeyId), the Razorpay Checkout
    // widget needs it to open.
    @Value("${app.razorpay.key-id}")
    private String keyId;

    // Secret used server-side only: to create orders via the Razorpay API,
    // and to verify payment/webhook signatures. Never sent to the frontend.
    @Value("${app.razorpay.key-secret}")
    private String keySecret;

    // Secret configured on the Razorpay Dashboard's Webhooks screen — used
    // to verify that an incoming webhook call genuinely came from Razorpay
    // and wasn't spoofed by a third party hitting our public endpoint.
    @Value("${app.razorpay.webhook-secret}")
    private String webhookSecret;

    public String getKeyId() {
        return keyId;
    }

    public String getKeySecret() {
        return keySecret;
    }

    public String getWebhookSecret() {
        return webhookSecret;
    }
}

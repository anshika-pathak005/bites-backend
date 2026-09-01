package com.bites.config;

import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Builds the single RazorpayClient bean used everywhere we need to talk to
 * Razorpay's API (creating orders, fetching a payment to double-check its
 * amount/status during verification — see PaymentServiceImpl). Credentials
 * come from RazorpayProperties, which in turn come from .env — nothing here
 * is hardcoded, so switching from Razorpay's test-mode keys to live keys
 * later is just an environment-variable change, no code change.
 */
@Configuration
public class RazorpayConfig {

    private final RazorpayProperties razorpayProperties;

    public RazorpayConfig(RazorpayProperties razorpayProperties) {
        this.razorpayProperties = razorpayProperties;
    }

    @Bean
    public RazorpayClient razorpayClient() throws RazorpayException {
        return new RazorpayClient(razorpayProperties.getKeyId(), razorpayProperties.getKeySecret());
    }
}

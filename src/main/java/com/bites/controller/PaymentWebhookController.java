package com.bites.controller;

import com.bites.service.PaymentService;
import com.bites.util.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/*
 * Called directly by Razorpay's servers (NOT the frontend) whenever a
 * payment's status changes — this is the reliability safety net for the
 * verify-payment flow: if the customer's browser closes/loses network right
 * after paying, this is what still lets us find out the payment went
 * through. Public (see SecurityConfig — no JWT/guest cookie makes sense
 * here, Razorpay isn't a logged-in browser), but the payload's signature
 * (verified inside PaymentService) is what proves it's genuinely from
 * Razorpay and not a forged call from anyone else who finds this URL.
 *
 * Configure this URL on the Razorpay Dashboard under Settings > Webhooks —
 * e.g. https://your-domain.com/api/payments/webhook/razorpay — and enable at
 * least the "payment.captured" and "payment.failed" events.
 */
@Slf4j
@RestController
@RequestMapping("/api/payments/webhook")
@RequiredArgsConstructor
public class PaymentWebhookController {

    private final PaymentService paymentService;

    // The raw request body is read as a plain String (not deserialized into
    // a DTO) because Razorpay's signature is computed over the exact raw
    // JSON bytes — parsing it into an object first and re-serializing it
    // for verification would risk a whitespace/key-ordering mismatch and
    // fail signature checks that should actually pass.
    @PostMapping("/razorpay")
    public ApiResponse<Void> handleRazorpayWebhook(@RequestBody String rawPayload,
                                                     @RequestHeader("X-Razorpay-Signature") String signature,
                                                     HttpServletRequest httpRequest) {
        log.info("Received Razorpay webhook call from {}", httpRequest.getRemoteAddr());
        paymentService.handleWebhook(rawPayload, signature);
        return ApiResponse.success(null, "Webhook processed");
    }
}

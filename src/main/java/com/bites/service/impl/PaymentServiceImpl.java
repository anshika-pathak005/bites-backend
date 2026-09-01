package com.bites.service.impl;

import com.bites.config.RazorpayProperties;
import com.bites.dto.request.PaymentVerificationRequest;
import com.bites.entity.Order;
import com.bites.entity.Payment;
import com.bites.entity.enums.PaymentMethod;
import com.bites.entity.enums.PaymentStatus;
import com.bites.exception.BadRequestException;
import com.bites.exception.ResourceNotFoundException;
import com.bites.repository.PaymentRepository;
import com.bites.service.PaymentService;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/*
 * Everything that talks to Razorpay lives here — order creation, signature
 * verification, and webhook handling — so OrderServiceImpl doesn't need to
 * know anything about Razorpay's API shape, just that "online payment"
 * means calling these 3 methods at the right points in the order lifecycle.
 *
 * Fully qualified references to com.razorpay.Order / com.razorpay.Payment
 * are used throughout (instead of importing them) because both names clash
 * with our own entity classes of the same name.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentServiceImpl implements PaymentService {

    // Razorpay considers a captured payment's status literally the string
    // "captured" — there's no enum for this on their SDK, so we compare
    // against the raw string from their API response.
    private static final String RAZORPAY_STATUS_CAPTURED = "captured";

    private final RazorpayClient razorpayClient;
    private final RazorpayProperties razorpayProperties;
    private final PaymentRepository paymentRepository;

    @Override
    @Transactional
    public Payment createOnlinePayment(Order order) {
        try {
            // Step 1:
            // Build the Razorpay order request using ONLY the amount our
            // own backend already computed and saved on this Order (see
            // OrderServiceImpl.checkout) — never anything the client sent.
            // Razorpay wants the amount in the smallest currency unit
            // (paise for INR), as a whole number.
            long amountInPaise = toPaise(order.getGrandTotal());

            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", amountInPaise);
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", order.getOrderNumber());
            // Auto-capture the payment the instant it's authorized, instead
            // of requiring a separate manual "capture" API call afterwards.
            orderRequest.put("payment_capture", 1);

            com.razorpay.Order razorpayOrder = razorpayClient.orders.create(orderRequest);
            String gatewayOrderId = razorpayOrder.get("id");

            // Step 2:
            // Save our own Payment row pointing at that Razorpay order —
            // this is what lets us find our way back to this order later,
            // from either the verify call or the webhook.
            Payment payment = Payment.builder()
                    .order(order)
                    .method(PaymentMethod.ONLINE)
                    .status(PaymentStatus.PENDING)
                    .gatewayOrderId(gatewayOrderId)
                    .build();

            log.info("Razorpay order created: orderNumber={}, gatewayOrderId={}", order.getOrderNumber(), gatewayOrderId);
            return paymentRepository.save(payment);
        } catch (RazorpayException e) {
            log.error("Failed to create Razorpay order for orderNumber={}", order.getOrderNumber(), e);
            throw new BadRequestException("Unable to initiate online payment right now. Please try again.");
        }
    }

    @Override
    @Transactional
    public Payment verifyAndCapture(PaymentVerificationRequest request) {
        // Step 1:
        // Find the Payment row this Razorpay order belongs to. If this
        // gatewayOrderId is unknown to us, someone is sending us garbage
        // (or a very stale/replayed request) — reject outright.
        Payment payment = paymentRepository.findByGatewayOrderId(request.razorpayOrderId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No payment found for this Razorpay order"));

        // Already verified (e.g. the webhook beat the frontend's verify
        // call here) — nothing to do, just return the current state
        // instead of re-verifying/erroring.
        if (payment.getStatus() == PaymentStatus.PAID) {
            return payment;
        }

        // Step 2:
        // Verify the HMAC signature Razorpay generated for this specific
        // (order_id, payment_id) pair, using our key secret. This is what
        // actually proves the payment_id genuinely belongs to Razorpay and
        // to THIS order — never trust the frontend's word alone.
        JSONObject attributes = new JSONObject();
        attributes.put("razorpay_order_id", request.razorpayOrderId());
        attributes.put("razorpay_payment_id", request.razorpayPaymentId());
        attributes.put("razorpay_signature", request.razorpaySignature());

        boolean signatureValid;
        try {
            signatureValid = Utils.verifyPaymentSignature(attributes, razorpayProperties.getKeySecret());
        } catch (RazorpayException e) {
            log.error("Error verifying Razorpay payment signature for gatewayOrderId={}", request.razorpayOrderId(), e);
            signatureValid = false;
        }

        if (!signatureValid) {
            failPayment(payment, "Signature verification failed");
            throw new BadRequestException("Payment verification failed — this payment could not be confirmed as genuine.");
        }

        // Step 3:
        // Belt-and-suspenders: fetch the payment directly from Razorpay's
        // API (not just trust the signature) and confirm it was actually
        // captured, and for the exact amount our order expects. Guards
        // against a captured-but-wrong-amount edge case, not just forged
        // signatures.
        try {
            com.razorpay.Payment razorpayPayment = razorpayClient.payments.fetch(request.razorpayPaymentId());
            String status = razorpayPayment.get("status");
            long paidAmountInPaise = Long.parseLong(razorpayPayment.get("amount").toString());
            long expectedAmountInPaise = toPaise(payment.getOrder().getGrandTotal());

            if (!RAZORPAY_STATUS_CAPTURED.equals(status)) {
                failPayment(payment, "Payment not captured (status: " + status + ")");
                throw new BadRequestException("Payment was not successfully captured.");
            }
            if (paidAmountInPaise != expectedAmountInPaise) {
                log.warn("Razorpay amount mismatch: expected={}, paid={}, gatewayOrderId={}",
                        expectedAmountInPaise, paidAmountInPaise, request.razorpayOrderId());
                failPayment(payment, "Paid amount did not match the order total");
                throw new BadRequestException("The paid amount does not match this order's total.");
            }
        } catch (RazorpayException e) {
            log.error("Error fetching Razorpay payment {} for confirmation", request.razorpayPaymentId(), e);
            throw new BadRequestException("Unable to confirm this payment right now. Please try again.");
        }

        // Step 4:
        // Everything checks out — mark it paid.
        payment.setStatus(PaymentStatus.PAID);
        payment.setTransactionId(request.razorpayPaymentId());
        payment.setPaidAt(LocalDateTime.now());
        payment.setFailureReason(null);

        log.info("Payment verified and captured: orderNumber={}, paymentId={}",
                payment.getOrder().getOrderNumber(), request.razorpayPaymentId());
        return paymentRepository.save(payment);
    }

    @Override
    @Transactional
    public void handleWebhook(String rawPayload, String signatureHeader) {
        // Step 1:
        // Verify this call genuinely came from Razorpay — anyone can POST
        // to a public webhook URL, so the signature (computed by Razorpay
        // over the exact raw JSON body, using the webhook secret configured
        // on their Dashboard) is the only thing that proves that.
        boolean signatureValid;
        try {
            signatureValid = Utils.verifyWebhookSignature(rawPayload, signatureHeader, razorpayProperties.getWebhookSecret());
        } catch (RazorpayException e) {
            log.error("Error verifying Razorpay webhook signature", e);
            signatureValid = false;
        }

        if (!signatureValid) {
            log.warn("Rejected webhook call with invalid signature");
            throw new BadRequestException("Invalid webhook signature");
        }

        JSONObject payload = new JSONObject(rawPayload);
        String event = payload.optString("event", "");

        // 1-arg optJSONObject (older org.json, bundled transitively via the
        // Razorpay SDK) returns null instead of a default when the key is
        // missing — so each level is null-checked by hand instead of
        // chaining the 2-arg default-value overload some newer org.json
        // versions have.
        JSONObject payloadObj = payload.optJSONObject("payload");
        JSONObject paymentObj = payloadObj != null ? payloadObj.optJSONObject("payment") : null;
        JSONObject paymentEntity = paymentObj != null ? paymentObj.optJSONObject("entity") : null;

        if (paymentEntity == null) {
            log.debug("Ignoring webhook event with no payment entity: event={}", event);
            return;
        }

        String gatewayOrderId = paymentEntity.optString("order_id", null);
        String razorpayPaymentId = paymentEntity.optString("id", null);

        if (gatewayOrderId == null) {
            log.debug("Ignoring webhook event with no order_id: event={}", event);
            return;
        }

        Payment payment = paymentRepository.findByGatewayOrderId(gatewayOrderId).orElse(null);
        if (payment == null) {
            // Not necessarily an error — could be a webhook for an order
            // created outside this environment (e.g. Razorpay's test
            // "Send Test Webhook" button). Log and move on.
            log.warn("Webhook for unknown gatewayOrderId={}, event={}", gatewayOrderId, event);
            return;
        }

        switch (event) {
            case "payment.captured" -> {
                // Idempotent: the frontend's verify call may have already
                // marked this PAID before the webhook arrived — don't
                // double-process.
                if (payment.getStatus() != PaymentStatus.PAID) {
                    payment.setStatus(PaymentStatus.PAID);
                    payment.setTransactionId(razorpayPaymentId);
                    payment.setPaidAt(LocalDateTime.now());
                    payment.setFailureReason(null);
                    paymentRepository.save(payment);
                    log.info("Payment marked PAID via webhook: orderNumber={}", payment.getOrder().getOrderNumber());
                }
            }
            case "payment.failed" -> {
                // Never downgrade an already-captured payment because of a
                // late/out-of-order failed-event delivery.
                if (payment.getStatus() != PaymentStatus.PAID) {
                    String reason = paymentEntity.optString("error_description", "Payment failed");
                    failPayment(payment, reason);
                    log.info("Payment marked FAILED via webhook: orderNumber={}, reason={}",
                            payment.getOrder().getOrderNumber(), reason);
                }
            }
            default -> log.debug("Ignoring unhandled webhook event: {}", event);
        }
    }

    // Internal helper: converts a rupee amount (e.g. 249.50) to the whole
    // number of paise Razorpay's API expects (e.g. 24950).
    private long toPaise(BigDecimal rupees) {
        return rupees.multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP).longValueExact();
    }

    // Internal helper: marks a payment FAILED with a reason, without
    // throwing — callers decide separately whether to also throw.
    private void failPayment(Payment payment, String reason) {
        payment.setStatus(PaymentStatus.FAILED);
        payment.setFailureReason(reason);
        paymentRepository.save(payment);
    }
}

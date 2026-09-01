package com.bites.entity;

import com.bites.entity.enums.PaymentMethod;
import com.bites.entity.enums.PaymentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * v1 only ever creates COD/PENDING rows. method and status are already
 * modeled as open enums so a real gateway (CARD/UPI/WALLET + a webhook
 * flipping status to PAID/FAILED) plugs in without a schema change.
 *
 * As of the Razorpay integration, ONLINE payments also flow through this
 * same table — see gatewayOrderId/failureReason below, and
 * PaymentServiceImpl for the create-order / verify / webhook logic.
 */
@Entity
@Table(name = "payment", uniqueConstraints = @UniqueConstraint(name = "uq_payment_order_id", columnNames = "order_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The order this payment is for (one payment per order).
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, foreignKey = @ForeignKey(name = "fk_payment_order"))
    private Order order;

    // How the customer paid; defaults to cash on delivery.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private PaymentMethod method = PaymentMethod.COD;

    // Current state of the payment; defaults to pending.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    // Reference id from the payment gateway, if any (not used for COD).
    // For Razorpay (method = ONLINE), this holds the PAYMENT id
    // (e.g. "pay_Hh..."), set only once the payment is verified as
    // successful — see PaymentServiceImpl.verifyAndCapture.
    @Column(name = "transaction_id", length = 150)
    private String transactionId;

    // Razorpay's ORDER id (e.g. "order_Hh..."), created up front — before
    // the customer has actually paid — so we have something to look this
    // payment back up by when Razorpay's webhook/verify call arrives later.
    // Null for COD payments, which never talk to a gateway at all.
    @Column(name = "gateway_order_id", length = 100)
    private String gatewayOrderId;

    // Why this payment ended up FAILED, if it did (e.g. "Payment declined
    // by bank") — surfaced from Razorpay's payment.failed webhook/verify
    // step, purely for admin/support visibility. Null otherwise.
    @Column(name = "failure_reason", length = 255)
    private String failureReason;

    // When the payment was confirmed as paid; null until then.
    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}

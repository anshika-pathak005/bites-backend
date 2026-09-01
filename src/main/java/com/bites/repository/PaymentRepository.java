package com.bites.repository;

import com.bites.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// Data access for payment records.
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    // Used to look up the payment linked to a specific order (e.g. checking payment status).
    Optional<Payment> findByOrderId(Long orderId);

    // Used by the Razorpay verify-payment and webhook flows to find which
    // Payment row a given Razorpay order corresponds to — gatewayOrderId is
    // the only thing both of those calls have in common with our DB.
    Optional<Payment> findByGatewayOrderId(String gatewayOrderId);
}

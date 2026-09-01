-- Adds online-payment (Razorpay) support to the existing payment table.
--
-- gateway_order_id: Razorpay's order id (e.g. "order_Hh...") — created and
-- stored BEFORE any money moves (see PaymentService.createRazorpayOrder), so
-- we can look the payment back up when the webhook/verify call arrives.
-- transaction_id (already existed) is reused for Razorpay's PAYMENT id
-- (e.g. "pay_Hh..."), set only after a successful payment.
--
-- failure_reason: human-readable reason a payment failed (from Razorpay's
-- payment.failed webhook / verify step), useful for admin/support visibility
-- into why a customer's payment didn't go through.
ALTER TABLE payment
    ADD COLUMN gateway_order_id VARCHAR(100) NULL,
    ADD COLUMN failure_reason   VARCHAR(255) NULL;

-- One Razorpay order should never be linked to more than one payment row.
ALTER TABLE payment
    ADD CONSTRAINT uq_payment_gateway_order_id UNIQUE (gateway_order_id);

-- Widen the allowed payment methods to include ONLINE (Razorpay — covers
-- card/UPI/wallet/netbanking all under one value, since Razorpay itself
-- decides the instrument; see PaymentMethod.java).
ALTER TABLE payment DROP CHECK chk_payment_method;
ALTER TABLE payment
    ADD CONSTRAINT chk_payment_method CHECK (method IN ('COD', 'CARD', 'UPI', 'WALLET', 'ONLINE'));

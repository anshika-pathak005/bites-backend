-- =====================================================================
-- Bites — Restaurant Ordering App
-- Flyway Migration V10: Customer login (email + password) alongside guest
--
-- Most of what this needs was already reserved earlier and never touched:
--   - customer.email / customer.password_hash existed since V1 ("unused in
--     v1, reserved for future login") — this migration just enforces email
--     uniqueness now that it's actually used to log in.
--   - cart.customer_id existed since V1 too (see the V1 comment: "guest
--     carts tracked by session_id; customer_id once logged in") and was
--     never dropped when V6 moved carts to guest_id — it's just been
--     sitting there unused. We only add a uniqueness guarantee (one cart
--     per logged-in customer, same as guest_id already has).
--
-- What's genuinely new:
--   - customer_address.customer_id — V6 dropped this when addresses moved
--     to pure guest_id; re-added here so a logged-in customer's saved
--     addresses can be looked up by customer_id instead.
--   - customer_refresh_token — mirrors the existing admin refresh_token
--     table, just for customer sessions instead of admin ones.
-- =====================================================================

ALTER TABLE customer
    ADD CONSTRAINT uq_customer_email UNIQUE (email);

ALTER TABLE cart
    ADD CONSTRAINT uq_cart_customer_id UNIQUE (customer_id);

ALTER TABLE customer_address
    ADD COLUMN customer_id BIGINT NULL;

ALTER TABLE customer_address
    ADD CONSTRAINT fk_customer_address_customer
    FOREIGN KEY (customer_id) REFERENCES customer(id)
    ON DELETE SET NULL;

CREATE INDEX idx_customer_address_customer_id ON customer_address(customer_id);

CREATE TABLE customer_refresh_token (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id     BIGINT          NOT NULL,
    token           VARCHAR(255)    NOT NULL,
    expiry_date     DATETIME        NOT NULL,
    revoked         BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_customer_refresh_token_token UNIQUE (token),
    CONSTRAINT fk_customer_refresh_token_customer FOREIGN KEY (customer_id) REFERENCES customer(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_customer_refresh_token_customer_id ON customer_refresh_token(customer_id);

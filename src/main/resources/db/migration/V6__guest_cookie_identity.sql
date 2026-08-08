-- =====================================================================
-- Bites — Restaurant Ordering App
-- Flyway Migration V6: Guest cookie identity
--
-- Replaces the client-supplied cart session id and phone-number-based
-- address lookup with a single server-generated, unguessable guest_id
-- (delivered as an HttpOnly cookie — see GuestIdentityFilter). No separate
-- "guest" table is needed: the cookie value itself is the identity, so
-- guest_id is just a plain indexed column, not a foreign key to anything.
-- =====================================================================

-- cart: rename session_id -> guest_id (same column, new meaning/source).
-- Length kept at 100 (unchanged) purely to avoid truncating any existing
-- session id values during the rename; new guest ids are ~43 chars either way.
ALTER TABLE cart
    CHANGE COLUMN session_id guest_id VARCHAR(100) NULL;

ALTER TABLE cart
    RENAME INDEX idx_cart_session_id TO idx_cart_guest_id;

-- customer_address: drop the old customer_id/phone-lookup link entirely —
-- address ownership is now purely guest_id, resolved from the HttpOnly
-- cookie (see AddressController). Add guest_id as the real ownership column.
ALTER TABLE customer_address
    DROP FOREIGN KEY fk_address_customer,
    DROP COLUMN customer_id,
    ADD COLUMN guest_id VARCHAR(64) NOT NULL,
    ADD INDEX idx_customer_address_guest_id (guest_id);

-- orders: add guest_id — powers "GET /api/orders" (list everything for
-- this browser). Order-level tracking/cancellation by a single order
-- continues to work by order NUMBER alone (see OrderController), so
-- guest_id here is only used for the list-all-mine view, not as an
-- authorization gate on individual order actions.
ALTER TABLE orders
    ADD COLUMN guest_id VARCHAR(64) NULL,
    ADD INDEX idx_orders_guest_id (guest_id);

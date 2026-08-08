-- =====================================================================
-- Bites — Restaurant Ordering App
-- Flyway Migration V5: Distance-based delivery pricing, order cancellation
-- window, and order tracking hardening.
--
-- Replaces the flat per-DeliveryArea delivery charge with configurable
-- distance slabs (see DeliveryChargeSlab), computed from the Haversine
-- distance between the restaurant and the delivery address. DeliveryArea
-- itself is left in place (admin screen still works) but is no longer
-- referenced by customer_address or orders — it's fully decoupled from
-- checkout/pricing as of this migration.
-- =====================================================================

-- ---------------------------------------------------------------------
-- 1. DELIVERY_CHARGE_SLAB — configurable distance -> flat fee table
-- ---------------------------------------------------------------------
CREATE TABLE delivery_charge_slab (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    min_distance_km     DECIMAL(4,2)    NOT NULL,
    max_distance_km     DECIMAL(4,2)    NOT NULL,
    charge              DECIMAL(10,2)   NOT NULL,
    is_active           BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT chk_slab_range CHECK (max_distance_km > min_distance_km)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Seed: the three slabs from the product spec. Safe to edit via the admin
-- "Delivery Pricing" screen afterwards — this is just a starting point so
-- checkout isn't broken on first boot.
INSERT INTO delivery_charge_slab (min_distance_km, max_distance_km, charge, is_active) VALUES
    (0.00, 2.00, 20.00, TRUE),
    (2.00, 5.00, 40.00, TRUE),
    (5.00, 8.00, 60.00, TRUE);

-- ---------------------------------------------------------------------
-- 2. CUSTOMER_ADDRESS — drop the delivery_area link entirely; pricing is
--    now computed from latitude/longitude alone at checkout time.
-- ---------------------------------------------------------------------
ALTER TABLE customer_address
    DROP FOREIGN KEY fk_address_delivery_area,
    DROP COLUMN delivery_area_id;

-- ---------------------------------------------------------------------
-- 3. ORDERS — same drop, plus a snapshot of the distance actually used to
--    price this order (useful for admin/support, and for auditing pricing
--    disputes without re-deriving it from a since-changed address).
-- ---------------------------------------------------------------------
ALTER TABLE orders
    DROP FOREIGN KEY fk_orders_delivery_area,
    DROP COLUMN delivery_area_id,
    ADD COLUMN distance_km DECIMAL(6,3) NULL,
    ADD COLUMN pincode VARCHAR(10) NULL;

-- ---------------------------------------------------------------------
-- 4. RESTAURANT — configurable cancellation window (minutes) a PENDING
--    order can still be self-cancelled by the guest who placed it.
-- ---------------------------------------------------------------------
ALTER TABLE restaurant
    ADD COLUMN cancellation_window_minutes INT NOT NULL DEFAULT 5;

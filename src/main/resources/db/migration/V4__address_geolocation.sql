-- =====================================================================
-- Bites — Restaurant Ordering App
-- Flyway Migration V4: Guest address & delivery-radius validation
--
-- Adds lat/lng (+ pincode) to customer_address so the backend can work out
-- exactly how far a saved address is from the restaurant, and adds lat/lng
-- to the restaurant row itself so there is a fixed origin point to measure
-- from (see HaversineUtil + CustomerServiceImpl).
--
-- NOTE: existing addresses (created before this migration) will have NULL
-- lat/lng/pincode until the customer edits them. This is intentional —
-- backfilling coordinates for old free-text addresses would require manual
-- geocoding, so old rows are left alone rather than guessed at.
-- =====================================================================
ALTER TABLE customer_address
    ADD COLUMN pincode    VARCHAR(10)     NULL,
    ADD COLUMN latitude   DECIMAL(10,7)   NULL,
    ADD COLUMN longitude  DECIMAL(10,7)   NULL;

-- Restaurant needs its own fixed coordinate — this is the "origin" point
-- every customer address's distance is measured against (see
-- RestaurantSettingsRequest/Response + CustomerServiceImpl.validateDeliveryRadius).
ALTER TABLE restaurant
    ADD COLUMN latitude   DECIMAL(10,7)   NULL,
    ADD COLUMN longitude  DECIMAL(10,7)   NULL;

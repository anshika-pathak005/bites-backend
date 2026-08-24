-- Makes the delivery radius (previously hardcoded as 8km in
-- DeliveryPricingServiceImpl) admin-configurable on the Restaurant
-- Settings screen, same as cancellation_window_minutes / min_order_amount.
ALTER TABLE restaurant
    ADD COLUMN max_delivery_radius_km DECIMAL(4, 2) NOT NULL DEFAULT 8.00;

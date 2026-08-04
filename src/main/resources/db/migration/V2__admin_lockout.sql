-- =====================================================================
-- Bites — Restaurant Ordering App
-- Flyway Migration V2: Admin login lockout support
-- Tracks consecutive failed login attempts per admin account so it can be
-- temporarily locked, independent of the per-IP rate limiting enforced in
-- the application layer (LoginRateLimitFilter).
-- =====================================================================
ALTER TABLE admin
    ADD COLUMN failed_attempts INT NOT NULL DEFAULT 0,
    ADD COLUMN locked_until DATETIME NULL;

-- =====================================================================
-- Bites — Restaurant Ordering App
-- Flyway Migration V1: Initial schema
-- Target: MySQL 8.x, InnoDB, utf8mb4
-- =====================================================================

-- ---------------------------------------------------------------------
-- 1. RESTAURANT  (singleton row — one restaurant in v1)
-- ---------------------------------------------------------------------
CREATE TABLE restaurant (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    name                VARCHAR(150)    NOT NULL,
    description         VARCHAR(1000)   NULL,
    logo_url            VARCHAR(500)    NULL,
    email               VARCHAR(150)    NULL,
    phone               VARCHAR(20)     NULL,
    address             VARCHAR(500)    NULL,
    opening_time        TIME            NULL,
    closing_time        TIME            NULL,
    is_open             BOOLEAN         NOT NULL DEFAULT TRUE,
    min_order_amount    DECIMAL(10,2)   NOT NULL DEFAULT 0.00,
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------------------
-- 2. ADMIN
-- ---------------------------------------------------------------------
CREATE TABLE admin (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(150)    NOT NULL,
    email           VARCHAR(150)    NOT NULL,
    password_hash   VARCHAR(255)    NOT NULL,
    role            VARCHAR(30)     NOT NULL DEFAULT 'ADMIN',
    is_active       BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_admin_email UNIQUE (email),
    CONSTRAINT chk_admin_role CHECK (role IN ('ADMIN', 'SUPER_ADMIN'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------------------
-- 3. REFRESH_TOKEN  (admin sessions — hashed, revocable)
-- ---------------------------------------------------------------------
CREATE TABLE refresh_token (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    admin_id        BIGINT          NOT NULL,
    token           VARCHAR(255)    NOT NULL,
    expiry_date     DATETIME        NOT NULL,
    revoked         BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_refresh_token_token UNIQUE (token),
    CONSTRAINT fk_refresh_token_admin FOREIGN KEY (admin_id) REFERENCES admin(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_refresh_token_admin_id ON refresh_token(admin_id);

-- ---------------------------------------------------------------------
-- 4. CUSTOMER  (upserted by phone number during guest checkout)
-- ---------------------------------------------------------------------
CREATE TABLE customer (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(150)    NOT NULL,
    phone           VARCHAR(20)     NOT NULL,
    email           VARCHAR(150)    NULL,
    password_hash   VARCHAR(255)    NULL,          -- unused in v1, reserved for future login
    is_guest        BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_customer_phone UNIQUE (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------------------
-- 5. DELIVERY_AREA
-- ---------------------------------------------------------------------
CREATE TABLE delivery_area (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    name                VARCHAR(150)    NOT NULL,
    delivery_charge     DECIMAL(10,2)   NOT NULL DEFAULT 0.00,
    is_active           BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------------------
-- 6. CUSTOMER_ADDRESS
-- ---------------------------------------------------------------------
CREATE TABLE customer_address (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id         BIGINT          NOT NULL,
    delivery_area_id    BIGINT          NOT NULL,
    label               VARCHAR(50)     NULL,        -- "Home", "Work", "Other"
    full_address        VARCHAR(500)    NOT NULL,
    landmark            VARCHAR(255)    NULL,
    is_default          BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_address_customer FOREIGN KEY (customer_id) REFERENCES customer(id) ON DELETE CASCADE,
    CONSTRAINT fk_address_delivery_area FOREIGN KEY (delivery_area_id) REFERENCES delivery_area(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_address_customer_id ON customer_address(customer_id);
CREATE INDEX idx_address_delivery_area_id ON customer_address(delivery_area_id);

-- ---------------------------------------------------------------------
-- 7. CATEGORY
-- ---------------------------------------------------------------------
CREATE TABLE category (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(100)    NOT NULL,
    description     VARCHAR(500)    NULL,
    image_url       VARCHAR(500)    NULL,
    display_order   INT             NOT NULL DEFAULT 0,
    is_active       BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------------------
-- 8. MENU_ITEM
-- ---------------------------------------------------------------------
CREATE TABLE menu_item (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_id         BIGINT          NOT NULL,
    name                VARCHAR(150)    NOT NULL,
    description         VARCHAR(1000)   NULL,
    price               DECIMAL(10,2)   NOT NULL,
    image_url           VARCHAR(500)    NULL,
    is_veg              BOOLEAN         NOT NULL DEFAULT TRUE,
    is_available        BOOLEAN         NOT NULL DEFAULT TRUE,
    is_best_seller      BOOLEAN         NOT NULL DEFAULT FALSE,
    is_todays_special   BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_menu_item_category FOREIGN KEY (category_id) REFERENCES category(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_menu_item_category_id ON menu_item(category_id);
CREATE INDEX idx_menu_item_flags ON menu_item(is_available, is_best_seller, is_todays_special);
CREATE FULLTEXT INDEX idx_menu_item_search ON menu_item(name, description);

-- ---------------------------------------------------------------------
-- 9. CART  (guest carts tracked by session_id; customer_id once logged in)
-- ---------------------------------------------------------------------
CREATE TABLE cart (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id     BIGINT          NULL,
    session_id      VARCHAR(100)    NULL,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_cart_customer FOREIGN KEY (customer_id) REFERENCES customer(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE UNIQUE INDEX idx_cart_session_id ON cart(session_id);
CREATE INDEX idx_cart_customer_id ON cart(customer_id);

-- ---------------------------------------------------------------------
-- 10. CART_ITEM
-- ---------------------------------------------------------------------
CREATE TABLE cart_item (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    cart_id             BIGINT          NOT NULL,
    menu_item_id        BIGINT          NOT NULL,
    quantity            INT             NOT NULL DEFAULT 1,
    price_at_addition   DECIMAL(10,2)   NOT NULL,
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_cart_item_cart FOREIGN KEY (cart_id) REFERENCES cart(id) ON DELETE CASCADE,
    CONSTRAINT fk_cart_item_menu_item FOREIGN KEY (menu_item_id) REFERENCES menu_item(id),
    CONSTRAINT chk_cart_item_quantity CHECK (quantity > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_cart_item_cart_id ON cart_item(cart_id);
CREATE INDEX idx_cart_item_menu_item_id ON cart_item(menu_item_id);

-- ---------------------------------------------------------------------
-- 11. ORDERS  ("order" is a reserved word in MySQL — table named "orders")
--     Snapshots customer/address/price data at creation time.
-- ---------------------------------------------------------------------
CREATE TABLE orders (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_number        VARCHAR(30)     NOT NULL,
    customer_id         BIGINT          NOT NULL,
    customer_name       VARCHAR(150)    NOT NULL,
    customer_phone      VARCHAR(20)     NOT NULL,
    delivery_address    VARCHAR(500)    NOT NULL,
    landmark            VARCHAR(255)    NULL,
    delivery_area_id    BIGINT          NOT NULL,
    delivery_charge     DECIMAL(10,2)   NOT NULL,
    subtotal            DECIMAL(10,2)   NOT NULL,
    grand_total         DECIMAL(10,2)   NOT NULL,
    status              VARCHAR(30)     NOT NULL DEFAULT 'PENDING',
    placed_at           DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_orders_order_number UNIQUE (order_number),
    CONSTRAINT fk_orders_customer FOREIGN KEY (customer_id) REFERENCES customer(id),
    CONSTRAINT fk_orders_delivery_area FOREIGN KEY (delivery_area_id) REFERENCES delivery_area(id),
    CONSTRAINT chk_orders_status CHECK (
        status IN ('PENDING', 'ACCEPTED', 'PREPARING', 'OUT_FOR_DELIVERY', 'DELIVERED', 'CANCELLED')
    )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_orders_customer_id ON orders(customer_id);
CREATE INDEX idx_orders_customer_phone ON orders(customer_phone);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_placed_at ON orders(placed_at);

-- ---------------------------------------------------------------------
-- 12. ORDER_ITEM  (snapshots item name/price at time of order)
-- ---------------------------------------------------------------------
CREATE TABLE order_item (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id        BIGINT          NOT NULL,
    menu_item_id    BIGINT          NOT NULL,
    item_name       VARCHAR(150)    NOT NULL,
    item_price      DECIMAL(10,2)   NOT NULL,
    quantity        INT             NOT NULL,
    subtotal        DECIMAL(10,2)   NOT NULL,
    CONSTRAINT fk_order_item_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_order_item_menu_item FOREIGN KEY (menu_item_id) REFERENCES menu_item(id),
    CONSTRAINT chk_order_item_quantity CHECK (quantity > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_order_item_order_id ON order_item(order_id);
CREATE INDEX idx_order_item_menu_item_id ON order_item(menu_item_id);

-- ---------------------------------------------------------------------
-- 13. PAYMENT  (1:1 with orders; shaped for future gateways)
-- ---------------------------------------------------------------------
CREATE TABLE payment (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id        BIGINT          NOT NULL,
    method          VARCHAR(30)     NOT NULL DEFAULT 'COD',
    status          VARCHAR(30)     NOT NULL DEFAULT 'PENDING',
    transaction_id  VARCHAR(150)    NULL,
    paid_at         DATETIME        NULL,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_payment_order_id UNIQUE (order_id),
    CONSTRAINT fk_payment_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT chk_payment_method CHECK (method IN ('COD', 'CARD', 'UPI', 'WALLET')),
    CONSTRAINT chk_payment_status CHECK (status IN ('PENDING', 'PAID', 'FAILED', 'REFUNDED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =====================================================================
-- Seed: singleton restaurant row + a starter delivery area
-- (Safe to remove/adjust — placeholder so the app has a config row on first boot)
-- =====================================================================
INSERT INTO restaurant (name, description, email, phone, address, opening_time, closing_time, is_open, min_order_amount)
VALUES ('Bites', 'Modern organic neighborhood bistro', 'hello@bites.example', '+91-0000000000', 'To be configured', '09:00:00', '23:00:00', TRUE, 0.00);

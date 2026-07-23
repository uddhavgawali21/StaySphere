-- =========================================================
-- STAYSPHERE — FINAL SCHEMA (Database First)
-- This is the single source of truth for the DB.
-- Spring Boot uses ddl-auto=validate against this schema.
-- Do NOT let Hibernate generate/alter tables.
-- =========================================================


CREATE DATABASE IF NOT EXISTS staysphere_db
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE staysphere_db;

-- =========================================================
-- USERS
-- Role:           ADMIN, OWNER, TENANT
-- AccountStatus:  ACTIVE, SUSPENDED, DEACTIVATED
-- =========================================================
CREATE TABLE users (
    user_id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name      VARCHAR(50)  NOT NULL,
    last_name       VARCHAR(50)  NOT NULL,
    email           VARCHAR(150) NOT NULL UNIQUE,
    phone           VARCHAR(15)  NOT NULL UNIQUE,
    password        VARCHAR(255) NOT NULL,
    role            VARCHAR(20)  NOT NULL,
    account_status  VARCHAR(20)  NOT NULL,
    created_at      DATETIME     NOT NULL,
    updated_at      DATETIME     NOT NULL
);

-- =========================================================
-- PROPERTIES
-- PropertyType:    ROOM, PG, FLAT, HOSTEL
-- OccupancyType:   SINGLE, SHARED
-- PropertyStatus:  ACTIVE, INACTIVE
-- =========================================================
CREATE TABLE properties (
    property_id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    owner_id         BIGINT NOT NULL,
    title            VARCHAR(150) NOT NULL,
    description      TEXT,
    property_type    VARCHAR(30)  NOT NULL,
    rent_amount      DECIMAL(10,2) NOT NULL,
    deposit_amount   DECIMAL(10,2) NOT NULL,
    occupancy_type   VARCHAR(20)  NOT NULL,
    address_line     VARCHAR(255) NOT NULL,
    area             VARCHAR(100) NOT NULL,
    city             VARCHAR(100) NOT NULL,
    state            VARCHAR(100) NOT NULL,
    pincode          VARCHAR(10)  NOT NULL,
    property_status  VARCHAR(20)  NOT NULL,
    created_at       DATETIME     NOT NULL,
    updated_at       DATETIME     NOT NULL,
    CONSTRAINT fk_property_owner
        FOREIGN KEY (owner_id) REFERENCES users(user_id)
        ON DELETE RESTRICT
);

-- =========================================================
-- PROPERTY IMAGES
-- =========================================================
CREATE TABLE property_images (
    image_id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    property_id   BIGINT NOT NULL,
    image_url     VARCHAR(500) NOT NULL,
    is_primary    BOOLEAN NOT NULL DEFAULT FALSE,
    uploaded_at   DATETIME NOT NULL,
    CONSTRAINT fk_image_property
        FOREIGN KEY (property_id) REFERENCES properties(property_id)
        ON DELETE CASCADE
);

-- =========================================================
-- FACILITIES
-- No master facility table (by design) — free-text per property
-- =========================================================
CREATE TABLE facilities (
    facility_id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    property_id    BIGINT NOT NULL,
    facility_name  VARCHAR(100) NOT NULL,
    created_at     DATETIME NOT NULL,
    CONSTRAINT fk_facility_property
        FOREIGN KEY (property_id) REFERENCES properties(property_id)
        ON DELETE CASCADE
);

-- =========================================================
-- BOOKINGS
-- BookingStatus: REQUESTED, PAYMENT_PENDING, CONFIRMED, REJECTED, CANCELLED
-- =========================================================
CREATE TABLE bookings (
    booking_id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    property_id     BIGINT NOT NULL,
    tenant_id       BIGINT NOT NULL,
    booking_status  VARCHAR(20) NOT NULL,
    request_date    DATETIME NOT NULL,
    move_in_date    DATE NOT NULL,
    created_at      DATETIME NOT NULL,
    updated_at      DATETIME NOT NULL,
    CONSTRAINT fk_booking_property
        FOREIGN KEY (property_id) REFERENCES properties(property_id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_booking_tenant
        FOREIGN KEY (tenant_id) REFERENCES users(user_id)
        ON DELETE RESTRICT
);

-- =========================================================
-- TRANSACTIONS
-- Lives in the monolith for now (Module 4).
-- PaymentStatus: INITIATED, SUCCESS, FAILED, REFUNDED
-- NOTE: table intentionally named "transactions" — do not rename.
-- =========================================================
CREATE TABLE transactions (
    transaction_id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_id        BIGINT NOT NULL,
    transaction_ref   VARCHAR(100) NOT NULL UNIQUE,
    amount            DECIMAL(10,2) NOT NULL,
    payment_method    VARCHAR(20) NOT NULL,
    payment_status    VARCHAR(20) NOT NULL,
    payment_date      DATETIME,
    CONSTRAINT fk_transaction_booking
        FOREIGN KEY (booking_id) REFERENCES bookings(booking_id)
        ON DELETE RESTRICT
);

-- =========================================================
-- INDEXES
-- =========================================================
CREATE INDEX idx_property_owner   ON properties(owner_id);
CREATE INDEX idx_booking_property ON bookings(property_id);
CREATE INDEX idx_booking_tenant   ON bookings(tenant_id);
CREATE INDEX idx_payment_booking  ON transactions(booking_id);
CREATE INDEX idx_city             ON properties(city);
CREATE INDEX idx_area             ON properties(area);
CREATE INDEX idx_property_status  ON properties(property_status);
CREATE INDEX idx_property_title   ON properties(title);
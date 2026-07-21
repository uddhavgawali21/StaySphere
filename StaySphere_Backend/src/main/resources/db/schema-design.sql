 CREATE DATABASE staysphere_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;


-- =========================================================
-- STAYSPHERE CORE DATABASE — DESIGN REFERENCE
-- Owned by: Spring Boot service
-- NOT auto-executed. JPA/Hibernate will generate the actual
-- schema from entities starting Step 3. This file documents
-- the target design and is used for review/diagramming.
-- =========================================================

-- USERS
use staysphere_db;
CREATE TABLE users (
    user_id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name      VARCHAR(50)  NOT NULL,
    last_name       VARCHAR(50)  NOT NULL,
    email           VARCHAR(150) NOT NULL UNIQUE,
    phone           VARCHAR(15)  NOT NULL UNIQUE,
    password        VARCHAR(255) NOT NULL,
    role            VARCHAR(20)  NOT NULL,   -- ADMIN, OWNER, TENANT
    account_status  VARCHAR(20)  NOT NULL,   -- ACTIVE, SUSPENDED, DEACTIVATED
    created_at      DATETIME     NOT NULL,
    updated_at      DATETIME     NOT NULL
);

-- PROPERTIES
CREATE TABLE properties (
    property_id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    owner_id         BIGINT NOT NULL,
    title            VARCHAR(150) NOT NULL,
    description      TEXT,
    property_type    VARCHAR(30)  NOT NULL,  -- PG, SINGLE_ROOM, FLAT, etc.
    rent_amount      DECIMAL(10,2) NOT NULL,
    deposit_amount   DECIMAL(10,2) NOT NULL,
    occupancy_type   VARCHAR(20)  NOT NULL,  -- SINGLE, SHARED, etc.
    address_line     VARCHAR(255) NOT NULL,
    area             VARCHAR(100) NOT NULL,
    city             VARCHAR(100) NOT NULL,
    pincode          VARCHAR(10)  NOT NULL,
    property_status  VARCHAR(20)  NOT NULL,  -- AVAILABLE, OCCUPIED, INACTIVE
    created_at       DATETIME     NOT NULL,
    updated_at       DATETIME     NOT NULL,
    CONSTRAINT fk_property_owner
        FOREIGN KEY (owner_id) REFERENCES users(user_id)
        ON DELETE RESTRICT
);

-- PROPERTY IMAGES
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

-- FACILITIES
CREATE TABLE facilities (
    facility_id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    property_id    BIGINT NOT NULL,
    facility_name  VARCHAR(100) NOT NULL,
    created_at     DATETIME NOT NULL,
    CONSTRAINT fk_facility_property
        FOREIGN KEY (property_id) REFERENCES properties(property_id)
        ON DELETE CASCADE
);

-- BOOKINGS
CREATE TABLE bookings (
    booking_id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    property_id     BIGINT NOT NULL,
    tenant_id       BIGINT NOT NULL,
    booking_status  VARCHAR(20) NOT NULL,   -- PENDING, APPROVED, REJECTED, CANCELLED
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

-- TRANSACTIONS (owned by Spring Boot; Payment microservice references booking_id externally, no cross-DB FK)
CREATE TABLE transactions (
    transaction_id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_id        BIGINT NOT NULL,
    transaction_ref   VARCHAR(100) NOT NULL UNIQUE,
    amount            DECIMAL(10,2) NOT NULL,
    payment_method    VARCHAR(20) NOT NULL,  -- UPI, CARD, NETBANKING, CASH
    payment_status    VARCHAR(20) NOT NULL,  -- INITIATED, SUCCESS, FAILED, REFUNDED
    payment_date      DATETIME,
    CONSTRAINT fk_transaction_booking
        FOREIGN KEY (booking_id) REFERENCES bookings(booking_id)
        ON DELETE RESTRICT
);
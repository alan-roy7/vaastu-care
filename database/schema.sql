-- Vaastu Care - MySQL 8 schema
-- Import with:  mysql -u root -p < database/schema.sql   (or run DbSetup, see README)
-- Safe to run more than once: nothing is dropped.

CREATE DATABASE IF NOT EXISTS vaastu_care CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE vaastu_care;

-- One row per account (owner, provider or admin). Passwords are stored as PBKDF2 hashes.
CREATE TABLE IF NOT EXISTS users (
    user_id       INT AUTO_INCREMENT PRIMARY KEY,
    role          ENUM('OWNER','PROVIDER','ADMIN') NOT NULL,
    full_name     VARCHAR(120) NOT NULL,
    email         VARCHAR(150) NOT NULL,
    username      VARCHAR(50)  NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    phone         VARCHAR(30)  NOT NULL,
    city          VARCHAR(80)  NOT NULL,
    created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_users_email (email),
    UNIQUE KEY uq_users_username (username),
    INDEX idx_users_role (role)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS categories (
    category_id INT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(80)  NOT NULL,
    description VARCHAR(255) NOT NULL,
    is_active   BOOLEAN NOT NULL DEFAULT TRUE,
    UNIQUE KEY uq_categories_name (name)
) ENGINE=InnoDB;

-- Extra details for users whose role is PROVIDER (one row per provider).
CREATE TABLE IF NOT EXISTS providers (
    user_id          INT PRIMARY KEY,
    category_id      INT NOT NULL,
    service_area     VARCHAR(200) NOT NULL,
    experience_years INT NOT NULL DEFAULT 0,
    description      TEXT NOT NULL,
    availability     VARCHAR(150) NOT NULL,
    approval_status  ENUM('Pending','Approved','Rejected') NOT NULL DEFAULT 'Pending',
    rejection_reason VARCHAR(255) NOT NULL DEFAULT '',
    is_suspended     BOOLEAN NOT NULL DEFAULT FALSE,
    applied_on       DATE NOT NULL,
    CONSTRAINT fk_providers_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_providers_category FOREIGN KEY (category_id) REFERENCES categories(category_id),
    CONSTRAINT chk_providers_experience CHECK (experience_years >= 0),
    INDEX idx_providers_category (category_id),
    INDEX idx_providers_approval (approval_status)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS properties (
    property_id   INT AUTO_INCREMENT PRIMARY KEY,
    owner_id      INT NOT NULL,
    name          VARCHAR(120) NOT NULL,
    address       VARCHAR(200) NOT NULL,
    city          VARCHAR(80)  NOT NULL,
    property_type VARCHAR(40)  NOT NULL,
    area_sqft     INT NOT NULL,
    bedrooms      INT NOT NULL DEFAULT 0,
    notes         TEXT NOT NULL,
    image_seed    INT NOT NULL DEFAULT 0,
    created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_properties_owner FOREIGN KEY (owner_id) REFERENCES users(user_id),
    CONSTRAINT chk_properties_area CHECK (area_sqft > 0),
    INDEX idx_properties_owner (owner_id)
) ENGINE=InnoDB AUTO_INCREMENT=1;

-- The owner of a request is the owner of its property. Request ids start at 1001 and are shown as SR-1001.
CREATE TABLE IF NOT EXISTS service_requests (
    request_id     INT AUTO_INCREMENT PRIMARY KEY,
    property_id    INT NOT NULL,
    provider_id    INT NOT NULL,
    category_id    INT NOT NULL,
    description    TEXT NOT NULL,
    notes          TEXT NOT NULL,
    preferred_date DATE NOT NULL,
    created_on     DATE NOT NULL,
    status         ENUM('Scheduled','In Progress','Completed','Rejected') NOT NULL DEFAULT 'Scheduled',
    is_accepted    BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_requests_property FOREIGN KEY (property_id) REFERENCES properties(property_id),
    CONSTRAINT fk_requests_provider FOREIGN KEY (provider_id) REFERENCES providers(user_id),
    CONSTRAINT fk_requests_category FOREIGN KEY (category_id) REFERENCES categories(category_id),
    INDEX idx_requests_property (property_id),
    INDEX idx_requests_provider (provider_id),
    INDEX idx_requests_status (status)
) ENGINE=InnoDB AUTO_INCREMENT=1001;

CREATE TABLE IF NOT EXISTS request_status_history (
    history_id INT AUTO_INCREMENT PRIMARY KEY,
    request_id INT NOT NULL,
    status     ENUM('Scheduled','In Progress','Completed','Rejected') NOT NULL,
    note       VARCHAR(500) NOT NULL,
    changed_by INT NOT NULL,
    changed_at DATETIME NOT NULL,
    CONSTRAINT fk_history_request FOREIGN KEY (request_id) REFERENCES service_requests(request_id) ON DELETE CASCADE,
    CONSTRAINT fk_history_user FOREIGN KEY (changed_by) REFERENCES users(user_id),
    INDEX idx_history_request (request_id)
) ENGINE=InnoDB;

-- photo_ref is DEMO:n (generated sample image) or FILE:uploads/name.jpg (picture stored in the uploads folder).
CREATE TABLE IF NOT EXISTS request_photos (
    photo_id   INT AUTO_INCREMENT PRIMARY KEY,
    request_id INT NOT NULL,
    photo_ref  VARCHAR(500) NOT NULL,
    added_at   DATETIME NOT NULL,
    CONSTRAINT fk_photos_request FOREIGN KEY (request_id) REFERENCES service_requests(request_id) ON DELETE CASCADE,
    INDEX idx_photos_request (request_id)
) ENGINE=InnoDB;

-- request_id is NULL for older reviews that are not linked to a request. A request can be reviewed only once.
CREATE TABLE IF NOT EXISTS reviews (
    review_id   INT AUTO_INCREMENT PRIMARY KEY,
    request_id  INT NULL,
    owner_id    INT NOT NULL,
    provider_id INT NOT NULL,
    rating      TINYINT NOT NULL,
    review_text VARCHAR(2000) NOT NULL,
    review_date DATE NOT NULL,
    UNIQUE KEY uq_reviews_request (request_id),
    CONSTRAINT fk_reviews_request FOREIGN KEY (request_id) REFERENCES service_requests(request_id),
    CONSTRAINT fk_reviews_owner FOREIGN KEY (owner_id) REFERENCES users(user_id),
    CONSTRAINT fk_reviews_provider FOREIGN KEY (provider_id) REFERENCES providers(user_id),
    CONSTRAINT chk_reviews_rating CHECK (rating BETWEEN 1 AND 5),
    INDEX idx_reviews_provider (provider_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS notifications (
    notification_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id         INT NOT NULL,
    message         VARCHAR(300) NOT NULL,
    request_id      INT NULL,
    created_at      DATETIME NOT NULL,
    is_read         BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_notifications_request FOREIGN KEY (request_id) REFERENCES service_requests(request_id) ON DELETE SET NULL,
    INDEX idx_notifications_user (user_id, is_read)
) ENGINE=InnoDB;

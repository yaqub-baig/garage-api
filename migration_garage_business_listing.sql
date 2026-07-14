-- ============================================
-- Garage Business For Sale Listing
-- Database Schema Migration
-- ============================================

-- Table: garage_business_listing
-- A garage owner's listing of their garage business for sale
CREATE TABLE garage_business_listing (
    id INT AUTO_INCREMENT PRIMARY KEY,
    garage_id INT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(2000),
    reason_for_selling VARCHAR(1000),
    asking_price DOUBLE NOT NULL,
    is_negotiable BOOLEAN DEFAULT FALSE,
    status VARCHAR(50) NOT NULL,
    listed_date DATE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    CONSTRAINT fk_garage_business_listing_garage
        FOREIGN KEY (garage_id) REFERENCES garage(id)
        ON DELETE CASCADE,
    INDEX idx_garage_business_listing_garage_id (garage_id),
    INDEX idx_garage_business_listing_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Note: the unidirectional @OneToMany(@JoinColumn(name="listing_images")) on
-- GarageBusinessListing.listingImages puts the FK column on the MANY side
-- (media_file.listing_images -> garage_business_listing.id), not here, matching
-- how Garage.garageImages/tradeLicenseAttachment/vatCertificate already work.
-- Hibernate (ddl-auto=update) adds that column to media_file automatically.

-- Table: garage_business_sale
-- Created when a listing is sold; records the buyer and sale terms
CREATE TABLE garage_business_sale (
    id INT AUTO_INCREMENT PRIMARY KEY,
    listing_id INT NOT NULL,
    buyer_id INT NOT NULL,
    sale_price DOUBLE NOT NULL,
    payment_mode VARCHAR(50),
    sale_date DATE,
    notes VARCHAR(1000),
    is_active BOOLEAN DEFAULT TRUE,
    created_at DATETIME NOT NULL,
    CONSTRAINT fk_garage_business_sale_listing
        FOREIGN KEY (listing_id) REFERENCES garage_business_listing(id)
        ON DELETE CASCADE,
    CONSTRAINT uq_garage_business_sale_listing
        UNIQUE (listing_id),
    INDEX idx_garage_business_sale_buyer_id (buyer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: garage_business_sale_invoice
-- Invoice generated for a finalized garage business sale
CREATE TABLE garage_business_sale_invoice (
    id INT AUTO_INCREMENT PRIMARY KEY,
    sale_id INT NOT NULL,
    invoice_number VARCHAR(100) UNIQUE,
    invoice_date DATE,
    total_amount DOUBLE,
    payment_method VARCHAR(50),
    created_at DATETIME NOT NULL,
    CONSTRAINT fk_garage_business_sale_invoice_sale
        FOREIGN KEY (sale_id) REFERENCES garage_business_sale(id)
        ON DELETE CASCADE,
    CONSTRAINT uq_garage_business_sale_invoice_sale
        UNIQUE (sale_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Sample data comments for testing
-- Uncomment below to insert test data
/*
INSERT INTO garage_business_listing (
    garage_id, title, description, asking_price, is_negotiable,
    status, listed_date, created_at, updated_at
)
VALUES (
    1, 'Established garage business for sale', 'Well-established garage with loyal customer base.',
    250000, TRUE, 'ACTIVE', CURDATE(), NOW(), NOW()
);
*/

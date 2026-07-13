-- ============================================
-- Garage Working Tools / Equipment Listing
-- Database Schema Migration
-- ============================================

-- Table: working_tools
-- Links garage to the tools/equipment it owns
CREATE TABLE working_tools (
    id INT AUTO_INCREMENT PRIMARY KEY,
    garage_id INT NOT NULL,
    tool_name VARCHAR(255) NOT NULL,
    category VARCHAR(100),
    brand VARCHAR(100),
    model VARCHAR(100),
    quantity INT NOT NULL DEFAULT 1,
    tool_condition VARCHAR(50),
    tool_image_id BIGINT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    notes VARCHAR(1000),
    CONSTRAINT fk_working_tools_garage
        FOREIGN KEY (garage_id) REFERENCES garage(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_working_tools_media
        FOREIGN KEY (tool_image_id) REFERENCES media_file(id)
        ON DELETE SET NULL,
    INDEX idx_working_tools_garage_id (garage_id),
    INDEX idx_working_tools_is_active (is_active),
    INDEX idx_working_tools_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Sample data comments for testing
-- Uncomment below to insert test data
/*
INSERT INTO working_tools (
    garage_id, tool_name, category, brand, model,
    quantity, tool_condition, created_at, updated_at
)
VALUES (
    1, 'Impact Wrench', 'Power Tool', 'DeWalt', 'DCF899',
    2, 'GOOD', NOW(), NOW()
);
*/

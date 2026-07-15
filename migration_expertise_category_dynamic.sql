-- =============================================================================
-- Migration: Make Technician Expertise Category Dynamic
-- Description: Replaces the hardcoded ExpertiseCategory enum with an
--              admin-manageable expertise_category lookup table, backfills
--              existing technician_expertise rows against it, and swaps the
--              old expertise_category VARCHAR column for an FK column.
-- Author: AutoAdda Development Team
-- Date: 2026-07-15
--
-- NOTE: spring.jpa.hibernate.ddl-auto=update will auto-create the new
-- expertise_category table and the new expertise_category_id column on next
-- boot, but will NOT seed data, backfill existing rows, add the FK/unique
-- constraints, or drop the old column. Run this script manually against the
-- database before/at deploy time.
--
-- This script is idempotent / safe to re-run: every step checks current
-- state via information_schema before acting, so it can recover from a
-- partially-applied previous run.
-- =============================================================================

-- =============================================================================
-- STEP 1: Create expertise_category table (no-op if Hibernate already did)
-- =============================================================================

CREATE TABLE IF NOT EXISTS expertise_category (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,

    CONSTRAINT uk_expertise_category_name UNIQUE (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================================================
-- STEP 2: Seed the 15 previously-hardcoded enum values as rows
-- (preserves the original enum names so existing data maps cleanly;
-- admins can rename/add/deactivate categories afterwards via the API)
-- =============================================================================

INSERT INTO expertise_category (name, description, is_active, created_at, updated_at)
SELECT * FROM (
    SELECT 'ENGINE_SYSTEMS' AS name, NULL AS description, 1 AS is_active, NOW() AS created_at, NOW() AS updated_at UNION ALL
    SELECT 'TRANSMISSION', NULL, 1, NOW(), NOW() UNION ALL
    SELECT 'ELECTRICAL_SYSTEMS', NULL, 1, NOW(), NOW() UNION ALL
    SELECT 'BRAKE_SYSTEMS', NULL, 1, NOW(), NOW() UNION ALL
    SELECT 'SUSPENSION_STEERING', NULL, 1, NOW(), NOW() UNION ALL
    SELECT 'HVAC_CLIMATE_CONTROL', NULL, 1, NOW(), NOW() UNION ALL
    SELECT 'DIAGNOSTICS', NULL, 1, NOW(), NOW() UNION ALL
    SELECT 'HYBRID_EV_SYSTEMS', NULL, 1, NOW(), NOW() UNION ALL
    SELECT 'FUEL_SYSTEMS', NULL, 1, NOW(), NOW() UNION ALL
    SELECT 'EXHAUST_EMISSIONS', NULL, 1, NOW(), NOW() UNION ALL
    SELECT 'COOLING_SYSTEMS', NULL, 1, NOW(), NOW() UNION ALL
    SELECT 'WELDING_FABRICATION', NULL, 1, NOW(), NOW() UNION ALL
    SELECT 'BODY_CHASSIS', NULL, 1, NOW(), NOW() UNION ALL
    SELECT 'TIRE_WHEEL', NULL, 1, NOW(), NOW() UNION ALL
    SELECT 'PREVENTIVE_MAINTENANCE', NULL, 1, NOW(), NOW()
) AS seed
WHERE NOT EXISTS (
    SELECT 1 FROM expertise_category ec WHERE ec.name = seed.name
);

-- =============================================================================
-- STEP 3: Add the new FK column to technician_expertise (if not already added)
-- =============================================================================

SET @col_exists = (
    SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'technician_expertise'
      AND column_name = 'expertise_category_id'
);

SET @sql = IF(@col_exists = 0,
    'ALTER TABLE technician_expertise ADD COLUMN expertise_category_id INT NULL',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- =============================================================================
-- STEP 4: Auto-seed any category value still present in existing data that
-- wasn't covered by the static list above (typos, extra legacy values, etc.),
-- so nothing is left unmatched before backfilling.
-- Only runs if the old expertise_category column still exists.
-- =============================================================================

SET @old_col_exists = (
    SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'technician_expertise'
      AND column_name = 'expertise_category'
);

SET @sql = IF(@old_col_exists > 0,
    'INSERT INTO expertise_category (name, description, is_active, created_at, updated_at)
     SELECT DISTINCT TRIM(te.expertise_category), NULL, 1, NOW(), NOW()
     FROM technician_expertise te
     WHERE te.expertise_category IS NOT NULL
       AND TRIM(te.expertise_category) <> ''''
       AND NOT EXISTS (
           SELECT 1 FROM expertise_category ec
           WHERE ec.name = TRIM(te.expertise_category)
       )',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- =============================================================================
-- STEP 5: Backfill expertise_category_id from the old VARCHAR values
-- (only rows not already backfilled; only if old column still exists)
-- =============================================================================

SET @sql = IF(@old_col_exists > 0,
    'UPDATE technician_expertise te
     JOIN expertise_category ec ON ec.name = TRIM(te.expertise_category)
     SET te.expertise_category_id = ec.id
     WHERE te.expertise_category_id IS NULL',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Informational check: report how many rows are still unmatched before
-- enforcing NOT NULL in Step 6. Step 4's auto-seed should make this 0; if
-- it isn't, Step 6 will fail loudly (Error 1138) and you can inspect via:
-- SELECT * FROM technician_expertise WHERE expertise_category_id IS NULL;
SELECT COUNT(*) AS unmatched_expertise_rows
FROM technician_expertise
WHERE expertise_category_id IS NULL;

-- =============================================================================
-- STEP 6: Enforce NOT NULL now that existing rows are backfilled
-- =============================================================================

SET @sql = IF(
    (SELECT is_nullable FROM information_schema.columns
     WHERE table_schema = DATABASE() AND table_name = 'technician_expertise'
       AND column_name = 'expertise_category_id') = 'YES',
    'ALTER TABLE technician_expertise MODIFY COLUMN expertise_category_id INT NOT NULL',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- =============================================================================
-- STEP 7: Ensure a standalone index on technician_id exists BEFORE touching
-- uk_technician_category, so the fk_expertise_technician FK always has a
-- supporting index and the drop in Step 8 doesn't get rejected (Error 1553).
-- =============================================================================

SET @tech_id_index_exists = (
    SELECT COUNT(*) FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'technician_expertise'
      AND index_name = 'idx_expertise_technician'
);

SET @sql = IF(@tech_id_index_exists = 0,
    'CREATE INDEX idx_expertise_technician ON technician_expertise(technician_id)',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- =============================================================================
-- STEP 8: Drop old indexes referencing the VARCHAR column (guarded)
-- =============================================================================

SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.statistics
     WHERE table_schema = DATABASE() AND table_name = 'technician_expertise'
       AND index_name = 'uk_technician_category') > 0,
    'ALTER TABLE technician_expertise DROP INDEX uk_technician_category',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.statistics
     WHERE table_schema = DATABASE() AND table_name = 'technician_expertise'
       AND index_name = 'idx_expertise_category') > 0,
    'ALTER TABLE technician_expertise DROP INDEX idx_expertise_category',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.statistics
     WHERE table_schema = DATABASE() AND table_name = 'technician_expertise'
       AND index_name = 'idx_expertise_category_level') > 0,
    'ALTER TABLE technician_expertise DROP INDEX idx_expertise_category_level',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.statistics
     WHERE table_schema = DATABASE() AND table_name = 'technician_expertise'
       AND index_name = 'idx_expertise_search') > 0,
    'ALTER TABLE technician_expertise DROP INDEX idx_expertise_search',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- =============================================================================
-- STEP 9: Add FK + indexes + unique constraint on the new column (guarded)
-- =============================================================================

SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.table_constraints
     WHERE table_schema = DATABASE() AND table_name = 'technician_expertise'
       AND constraint_name = 'fk_technician_expertise_category') = 0,
    'ALTER TABLE technician_expertise
        ADD CONSTRAINT fk_technician_expertise_category
        FOREIGN KEY (expertise_category_id) REFERENCES expertise_category(id)',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.statistics
     WHERE table_schema = DATABASE() AND table_name = 'technician_expertise'
       AND index_name = 'idx_expertise_category_id') = 0,
    'CREATE INDEX idx_expertise_category_id ON technician_expertise(expertise_category_id)',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.statistics
     WHERE table_schema = DATABASE() AND table_name = 'technician_expertise'
       AND index_name = 'idx_expertise_category_level') = 0,
    'CREATE INDEX idx_expertise_category_level ON technician_expertise(expertise_category_id, expertise_level)',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.statistics
     WHERE table_schema = DATABASE() AND table_name = 'technician_expertise'
       AND index_name = 'idx_expertise_search') = 0,
    'CREATE INDEX idx_expertise_search ON technician_expertise(expertise_category_id, expertise_level, technician_id)',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.statistics
     WHERE table_schema = DATABASE() AND table_name = 'technician_expertise'
       AND index_name = 'uk_technician_category') = 0,
    'ALTER TABLE technician_expertise
        ADD CONSTRAINT uk_technician_category UNIQUE (technician_id, expertise_category_id)',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- =============================================================================
-- STEP 10: Drop the old VARCHAR column (guarded)
-- =============================================================================

SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.columns
     WHERE table_schema = DATABASE() AND table_name = 'technician_expertise'
       AND column_name = 'expertise_category') > 0,
    'ALTER TABLE technician_expertise DROP COLUMN expertise_category',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- =============================================================================
-- VERIFICATION QUERIES
-- =============================================================================

-- SELECT * FROM expertise_category ORDER BY id;
-- SHOW CREATE TABLE technician_expertise;
-- SELECT COUNT(*) FROM technician_expertise WHERE expertise_category_id IS NULL;

-- =============================================================================
-- ROLLBACK NOTES
-- =============================================================================
-- This migration is destructive to the old expertise_category VARCHAR column
-- (dropped in Step 10). To roll back, re-add the column, backfill it from
-- expertise_category.name via expertise_category_id, then drop the FK column.
-- =============================================================================

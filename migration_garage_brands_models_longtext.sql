-- =============================================================================
-- Migration: Change garage brands and models columns to LONGTEXT
-- Description: Increases storage capacity for garage.brands and garage.models
--              columns to avoid truncation errors for garages with long lists
-- Author: AutoAdda Development Team
-- Date: 2026-07-15
-- =============================================================================

ALTER TABLE garage
    MODIFY COLUMN brands LONGTEXT,
    MODIFY COLUMN models LONGTEXT;

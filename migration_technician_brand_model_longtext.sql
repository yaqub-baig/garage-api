-- =============================================================================
-- Migration: Change technician brand and model columns to LONGTEXT
-- Description: Increases storage capacity for technician.brand and
--              technician.model columns to avoid truncation errors for
--              technicians with long brand/model lists
-- Author: AutoAdda Development Team
-- Date: 2026-07-16
-- =============================================================================

ALTER TABLE technician
    MODIFY COLUMN brand LONGTEXT,
    MODIFY COLUMN model LONGTEXT;

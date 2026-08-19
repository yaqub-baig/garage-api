-- ============================================
-- Garage Tamara Payment Flag Migration
-- Description: Adds tamara_payment_available flag to garage table
-- ============================================

ALTER TABLE garage
ADD COLUMN tamara_payment_available BOOLEAN NOT NULL DEFAULT FALSE
COMMENT 'Whether this garage accepts Tamara (buy-now-pay-later) payments';

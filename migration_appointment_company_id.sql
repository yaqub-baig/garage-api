-- ============================================
-- Record which company (fleet / showroom) a booked vehicle belongs to
-- ============================================
-- NULL = privately owned vehicle. Set at booking time from vehicle-api's companyId.
-- Hibernate ddl-auto=update adds the column automatically; run this where schema
-- changes are applied by hand.

ALTER TABLE appointment_details
    ADD COLUMN company_id BIGINT NULL,
    ADD INDEX idx_appointment_details_company_id (company_id);

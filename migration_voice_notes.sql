-- ============================================
-- Voice notes on every note / comment / remark field
-- ============================================
-- Each column is an optional FK to media_file holding an audio recording
-- uploaded via POST /mediafiles/upload (stored under uploads/audios/).
-- Deleting the audio file clears the reference (ON DELETE SET NULL).
--
-- Hibernate ddl-auto=update adds the columns automatically; run this script
-- on environments where schema changes are applied by hand.

-- Notes
ALTER TABLE part_request
    ADD COLUMN voice_note_id BIGINT NULL,
    ADD CONSTRAINT fk_part_request_voice_note FOREIGN KEY (voice_note_id) REFERENCES media_file(id) ON DELETE SET NULL;

ALTER TABLE garage_procurement
    ADD COLUMN voice_note_id BIGINT NULL,
    ADD CONSTRAINT fk_garage_procurement_voice_note FOREIGN KEY (voice_note_id) REFERENCES media_file(id) ON DELETE SET NULL;

ALTER TABLE working_tools
    ADD COLUMN voice_note_id BIGINT NULL,
    ADD CONSTRAINT fk_working_tools_voice_note FOREIGN KEY (voice_note_id) REFERENCES media_file(id) ON DELETE SET NULL;

ALTER TABLE garage_inventory_item
    ADD COLUMN voice_note_id BIGINT NULL,
    ADD CONSTRAINT fk_garage_inventory_item_voice_note FOREIGN KEY (voice_note_id) REFERENCES media_file(id) ON DELETE SET NULL;

ALTER TABLE inventory_transaction
    ADD COLUMN voice_note_id BIGINT NULL,
    ADD CONSTRAINT fk_inventory_transaction_voice_note FOREIGN KEY (voice_note_id) REFERENCES media_file(id) ON DELETE SET NULL;

ALTER TABLE inventory_alert
    ADD COLUMN resolution_voice_note_id BIGINT NULL,
    ADD CONSTRAINT fk_inventory_alert_resolution_voice_note FOREIGN KEY (resolution_voice_note_id) REFERENCES media_file(id) ON DELETE SET NULL;

ALTER TABLE invoice
    ADD COLUMN voice_note_id BIGINT NULL,
    ADD CONSTRAINT fk_invoice_voice_note FOREIGN KEY (voice_note_id) REFERENCES media_file(id) ON DELETE SET NULL;

ALTER TABLE garage_business_sale
    ADD COLUMN voice_note_id BIGINT NULL,
    ADD CONSTRAINT fk_garage_business_sale_voice_note FOREIGN KEY (voice_note_id) REFERENCES media_file(id) ON DELETE SET NULL;

ALTER TABLE pickup_drop_request
    ADD COLUMN voice_note_id BIGINT NULL,
    ADD CONSTRAINT fk_pickup_drop_request_voice_note FOREIGN KEY (voice_note_id) REFERENCES media_file(id) ON DELETE SET NULL;

-- Comments / remarks
ALTER TABLE appointment_details
    ADD COLUMN voice_note_id BIGINT NULL,
    ADD CONSTRAINT fk_appointment_details_voice_note FOREIGN KEY (voice_note_id) REFERENCES media_file(id) ON DELETE SET NULL;

ALTER TABLE appointment_status_activity
    ADD COLUMN voice_note_id BIGINT NULL,
    ADD CONSTRAINT fk_appointment_status_activity_voice_note FOREIGN KEY (voice_note_id) REFERENCES media_file(id) ON DELETE SET NULL;

ALTER TABLE job_card
    ADD COLUMN voice_note_id BIGINT NULL,
    ADD CONSTRAINT fk_job_card_voice_note FOREIGN KEY (voice_note_id) REFERENCES media_file(id) ON DELETE SET NULL;

ALTER TABLE job_card_status_activity
    ADD COLUMN voice_note_id BIGINT NULL,
    ADD CONSTRAINT fk_job_card_status_activity_voice_note FOREIGN KEY (voice_note_id) REFERENCES media_file(id) ON DELETE SET NULL;

ALTER TABLE job_card_additional_request
    ADD COLUMN voice_note_id BIGINT NULL,
    ADD CONSTRAINT fk_job_card_additional_request_voice_note FOREIGN KEY (voice_note_id) REFERENCES media_file(id) ON DELETE SET NULL;

ALTER TABLE customer_part_response
    ADD COLUMN customer_voice_note_id BIGINT NULL,
    ADD CONSTRAINT fk_customer_part_response_voice_note FOREIGN KEY (customer_voice_note_id) REFERENCES media_file(id) ON DELETE SET NULL;

ALTER TABLE vehicle_received
    ADD COLUMN voice_note_id BIGINT NULL,
    ADD CONSTRAINT fk_vehicle_received_voice_note FOREIGN KEY (voice_note_id) REFERENCES media_file(id) ON DELETE SET NULL;

ALTER TABLE vehicle_delivery
    ADD COLUMN customer_voice_note_id BIGINT NULL,
    ADD CONSTRAINT fk_vehicle_delivery_customer_voice_note FOREIGN KEY (customer_voice_note_id) REFERENCES media_file(id) ON DELETE SET NULL;

ALTER TABLE work_order
    ADD COLUMN customer_negotiated_voice_note_id BIGINT NULL,
    ADD COLUMN garage_negotiated_voice_note_id BIGINT NULL,
    ADD CONSTRAINT fk_work_order_customer_negotiated_voice_note FOREIGN KEY (customer_negotiated_voice_note_id) REFERENCES media_file(id) ON DELETE SET NULL,
    ADD CONSTRAINT fk_work_order_garage_negotiated_voice_note FOREIGN KEY (garage_negotiated_voice_note_id) REFERENCES media_file(id) ON DELETE SET NULL;

ALTER TABLE roadside_assistance
    ADD COLUMN voice_note_id BIGINT NULL,
    ADD CONSTRAINT fk_roadside_assistance_voice_note FOREIGN KEY (voice_note_id) REFERENCES media_file(id) ON DELETE SET NULL;

ALTER TABLE insurance_claim
    ADD COLUMN approval_voice_note_id BIGINT NULL,
    ADD CONSTRAINT fk_insurance_claim_approval_voice_note FOREIGN KEY (approval_voice_note_id) REFERENCES media_file(id) ON DELETE SET NULL;

-- ============================================
-- Part request timeline: when each status / part event happened
-- ============================================
-- Hibernate ddl-auto=update creates these automatically; run this script on
-- environments where schema changes are applied by hand.
-- Part requests created before this have no rows here; their timeline falls back
-- to request_date and customer_response_date.

CREATE TABLE part_request_status_timestamps (
    part_request_id INT NOT NULL,
    status VARCHAR(40) NOT NULL,      -- PartRequestStatus
    timestamp DATETIME(6),
    PRIMARY KEY (part_request_id, status),
    CONSTRAINT fk_part_request_status_ts FOREIGN KEY (part_request_id) REFERENCES part_request(id) ON DELETE CASCADE
);

ALTER TABLE part_request_item
    ADD COLUMN received_at DATETIME(6) NULL,          -- customer handed the part over
    ADD COLUMN added_to_invoice_at DATETIME(6) NULL;  -- part added to the invoice

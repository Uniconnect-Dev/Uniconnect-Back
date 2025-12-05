ALTER TABLE receipt_confirmations
    ADD COLUMN received_quantity INTEGER,
    ADD COLUMN has_defect BOOLEAN,
    ADD COLUMN expiration_date DATE,
    ADD COLUMN received_at TIMESTAMP;
ALTER TABLE receipt_confirmations
ADD COLUMN signature_image_base64 TEXT;

ALTER TABLE receipt_confirmations
ADD COLUMN signature_timestamp BIGINT;

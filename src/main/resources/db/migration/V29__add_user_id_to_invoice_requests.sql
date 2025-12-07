-- InvoiceRequest 엔티티에 userId 필드 추가
ALTER TABLE invoice_requests
ADD COLUMN user_id BIGINT NOT NULL DEFAULT 1;

-- 기존 데이터 처리 후 DEFAULT 제거 가능
ALTER TABLE invoice_requests
ALTER COLUMN user_id DROP DEFAULT;

-- V52__add_transaction_id_to_payments.sql
-- Payment 엔티티에 거래 정보 관련 필드 추가

-- 1. transaction_id 컬럼 추가 (PG사 거래 ID)
ALTER TABLE payments
    ADD COLUMN IF NOT EXISTS transaction_id VARCHAR(100);

-- 2. receipt_url 컬럼 추가 (영수증 URL)
ALTER TABLE payments
    ADD COLUMN IF NOT EXISTS receipt_url TEXT;

-- 3. completed_at 컬럼 추가 (결제 완료 시간)
ALTER TABLE payments
    ADD COLUMN IF NOT EXISTS completed_at TIMESTAMP;

-- 4. canceled_at 컬럼 추가 (결제 취소 시간)
ALTER TABLE payments
    ADD COLUMN IF NOT EXISTS canceled_at TIMESTAMP;

-- 5. 기존 SUCCESS 상태의 결제는 created_at을 completed_at으로 설정
UPDATE payments
SET completed_at = created_at
WHERE status = 'SUCCESS' AND completed_at IS NULL;

-- 6. 기존 CANCELED 상태의 결제는 created_at을 canceled_at으로 설정
UPDATE payments
SET canceled_at = created_at
WHERE status = 'CANCELED' AND canceled_at IS NULL;

-- 7. transaction_id에 대한 인덱스 생성 (PG사 거래 조회 시 성능 향상)
CREATE INDEX IF NOT EXISTS idx_payments_transaction_id ON payments(transaction_id);

-- 8. status 인덱스 생성 (결제 상태별 조회 시 성능 향상)
CREATE INDEX IF NOT EXISTS idx_payments_status ON payments(status);

-- 9. company_id 인덱스 생성 (회사별 결제 조회 시 성능 향상)
CREATE INDEX IF NOT EXISTS idx_payments_company_id ON payments(company_id);
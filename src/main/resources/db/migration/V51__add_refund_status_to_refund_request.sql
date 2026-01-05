-- V51__add_refund_status_fields.sql
-- RefundRequest에 환불 상태 관련 필드 추가

-- 1. refund_status 컬럼이 없으면 추가
ALTER TABLE refund_requests
    ADD COLUMN IF NOT EXISTS refund_status VARCHAR(20) DEFAULT 'PENDING';

-- 2. refund_rejection_reason 컬럼 추가 (거절 사유)
ALTER TABLE refund_requests
    ADD COLUMN IF NOT EXISTS refund_rejection_reason TEXT;

-- 3. completed_at 컬럼 추가 (환불 완료 일시)
ALTER TABLE refund_requests
    ADD COLUMN IF NOT EXISTS completed_at TIMESTAMP;

-- 4. refund_transaction_id 컬럼 추가 (환불 거래 ID)
ALTER TABLE refund_requests
    ADD COLUMN IF NOT EXISTS refund_transaction_id VARCHAR(100);

-- 5. rejected_at 컬럼 추가 (거절 일시)
ALTER TABLE refund_requests
    ADD COLUMN IF NOT EXISTS rejected_at TIMESTAMP;

-- 6. 기존 데이터의 refund_status를 COMPLETED로 설정 (이미 환불 처리된 것으로 간주)
UPDATE refund_requests
SET refund_status = 'COMPLETED'
WHERE refund_status IS NULL OR refund_status = '';

-- 7. NOT NULL 제약조건 설정
ALTER TABLE refund_requests
    ALTER COLUMN refund_status SET NOT NULL;
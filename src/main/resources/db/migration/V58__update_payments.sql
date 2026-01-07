-- Payment 테이블 리팩토링
-- 1. campaign_id FK 제거
-- 2. completed_at, canceled_at, student_org_id, collaboration_match_request_id, transaction_id, receipt_url 추가

-- 기존 payments 테이블에서 campaign_id FK가 있다면 제거
ALTER TABLE payments
DROP CONSTRAINT IF EXISTS payments_campaign_id_fk;

ALTER TABLE payments
DROP COLUMN IF EXISTS campaign_id;

-- 새로운 컬럼 추가
ALTER TABLE payments
    ADD COLUMN IF NOT EXISTS completed_at TIMESTAMP;

ALTER TABLE payments
    ADD COLUMN IF NOT EXISTS canceled_at TIMESTAMP;

ALTER TABLE payments
    ADD COLUMN IF NOT EXISTS student_org_id BIGINT;

ALTER TABLE payments
    ADD COLUMN IF NOT EXISTS collaboration_match_request_id BIGINT NOT NULL;

ALTER TABLE payments
    ADD COLUMN IF NOT EXISTS transaction_id VARCHAR(100);

ALTER TABLE payments
    ADD COLUMN IF NOT EXISTS receipt_url TEXT;

-- Foreign Key 제약 조건 추가
ALTER TABLE payments
    ADD CONSTRAINT payments_student_org_id_fk
        FOREIGN KEY (student_org_id) REFERENCES student_orgs(student_org_id) ON DELETE SET NULL;

ALTER TABLE payments
    ADD CONSTRAINT payments_collaboration_match_request_id_fk
        FOREIGN KEY (collaboration_match_request_id) REFERENCES collaboration_match_requests(id) ON DELETE CASCADE;

-- 인덱스 생성
CREATE INDEX IF NOT EXISTS idx_payments_student_org_id
    ON payments(student_org_id);

CREATE INDEX IF NOT EXISTS idx_payments_collaboration_match_request_id
    ON payments(collaboration_match_request_id);

CREATE INDEX IF NOT EXISTS idx_payments_transaction_id
    ON payments(transaction_id);

-- 로그
-- ✅ Payment 테이블 리팩토링 완료
-- - campaign_id FK 제거
-- - completed_at, canceled_at 추가
-- - student_org_id FK 추가
-- - collaboration_match_request_id FK 추가 (NOT NULL)
-- - transaction_id 추가
-- - receipt_url 추가
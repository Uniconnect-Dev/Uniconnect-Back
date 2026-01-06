-- V57__update_payments_remove_campaign_add_collab_match_request.sql
-- 목적: payments 테이블에서 campaign_id 제거, collaboration_match_request_id 추가

-- 1) 기존 campaign_id FK 제약 제거
ALTER TABLE payments
DROP CONSTRAINT IF EXISTS fk_payments_campaign_id;

-- 2) campaign_id 칼럼 제거
ALTER TABLE payments
DROP COLUMN IF EXISTS campaign_id;

-- 3) collaboration_match_request_id 칼럼 추가
ALTER TABLE payments
    ADD COLUMN IF NOT EXISTS collaboration_match_request_id BIGINT NOT NULL;

-- 4) collaboration_match_request_id FK 제약 추가
ALTER TABLE payments
    ADD CONSTRAINT fk_payments_collaboration_match_request_id
        FOREIGN KEY (collaboration_match_request_id)
            REFERENCES collaboration_match_requests(id)
            ON DELETE CASCADE;

-- 5) 인덱스 생성 (조회 성능 향상)
CREATE INDEX IF NOT EXISTS idx_payments_collaboration_match_request_id
    ON payments(collaboration_match_request_id);

-- 6) 기존 인덱스 유지 (다른 조회용)
CREATE INDEX IF NOT EXISTS idx_payments_company_id_created_at
    ON payments(company_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_payments_student_org_id_created_at
    ON payments(student_org_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_payments_status
    ON payments(status);
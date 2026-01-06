-- =====================================================
-- 1. collaboration_type NOT NULL
-- =====================================================
ALTER TABLE collaboration_match_requests
ALTER COLUMN collaboration_type SET NOT NULL;

-- =====================================================
-- 2. status 값 정리
-- =====================================================
UPDATE collaboration_match_requests
SET status = 'Requested'
WHERE status IS NULL;

ALTER TABLE collaboration_match_requests
ALTER COLUMN status SET NOT NULL;

-- =====================================================
-- 3. status 체크 제약 추가
-- =====================================================
ALTER TABLE collaboration_match_requests
DROP CONSTRAINT IF EXISTS chk_match_status;

ALTER TABLE collaboration_match_requests
ADD CONSTRAINT chk_match_status
CHECK (status IN ('Requested', 'Approved', 'Rejected'));

-- =====================================================
-- 4. responded_at은 Approved / Rejected 시만 사용
-- (컬럼은 유지, 로직으로 제어)
-- =====================================================

-- =====================================================
-- 5. campaign_id NOT NULL 보장 (이미 데이터 있으면 OK)
-- =====================================================
ALTER TABLE collaboration_match_requests
ALTER COLUMN campaign_id SET NOT NULL;

-- =====================================================
-- 6. 중복 매칭 방지 (이미 있다면 skip)
-- =====================================================
CREATE UNIQUE INDEX IF NOT EXISTS uq_match_campaign_company
ON collaboration_match_requests (campaign_id, company_id);

-- 1. campaign_id FK 제거
ALTER TABLE sampling_reports
DROP CONSTRAINT IF EXISTS fk_sampling_report_campaign;

-- 2. campaign_id 컬럼 제거
ALTER TABLE sampling_reports
DROP COLUMN IF EXISTS campaign_id;

-- 3. collaboration_id 컬럼 추가
ALTER TABLE sampling_reports
ADD COLUMN collaboration_id BIGINT;

-- 4. NOT NULL (개발 단계 기준)
ALTER TABLE sampling_reports
ALTER COLUMN collaboration_id SET NOT NULL;

-- 5. collaboration FK
ALTER TABLE sampling_reports
ADD CONSTRAINT fk_sampling_report_collaboration
FOREIGN KEY (collaboration_id)
REFERENCES collaborations (collaboration_id)
ON DELETE CASCADE;

-- 6. 1:1 보장
CREATE UNIQUE INDEX uq_sampling_report_collaboration
ON sampling_reports (collaboration_id);

ALTER TABLE collaborations
DROP CONSTRAINT IF EXISTS fk_collaboration_matching;

ALTER TABLE collaborations
ADD CONSTRAINT fk_collaboration_matching
FOREIGN KEY (matching_id)
REFERENCES collaboration_match_requests (id)
ON DELETE CASCADE;

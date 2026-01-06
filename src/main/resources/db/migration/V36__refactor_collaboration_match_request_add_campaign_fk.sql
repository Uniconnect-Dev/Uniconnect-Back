-- 1. campaign_id 컬럼 추가
ALTER TABLE collaboration_match_requests
ADD COLUMN campaign_id BIGINT;

-- 2. 기존 불필요 컬럼 제거
ALTER TABLE collaboration_match_requests
DROP COLUMN IF EXISTS event_title,
DROP COLUMN IF EXISTS desired_date,
DROP COLUMN IF EXISTS industry;

ALTER TABLE collaboration_match_requests
ALTER COLUMN campaign_id SET NOT NULL,
ALTER COLUMN student_org_id SET NOT NULL,
ALTER COLUMN company_id SET NOT NULL;

-- campaign FK
ALTER TABLE collaboration_match_requests
ADD CONSTRAINT fk_match_campaign
FOREIGN KEY (campaign_id)
REFERENCES campaigns (campaign_id)
ON DELETE CASCADE;

-- student_org FK
ALTER TABLE collaboration_match_requests
ADD CONSTRAINT fk_match_student_org
FOREIGN KEY (student_org_id)
REFERENCES student_orgs (student_org_id)
ON DELETE CASCADE;

-- company FK
ALTER TABLE collaboration_match_requests
ADD CONSTRAINT fk_match_company
FOREIGN KEY (company_id)
REFERENCES companies (company_id)
ON DELETE CASCADE;


-- =====================================================
-- 6. 중복 매칭 방지 (캠페인 + 기업)
-- =====================================================
CREATE UNIQUE INDEX IF NOT EXISTS uq_match_campaign_company
ON collaboration_match_requests (campaign_id, company_id);
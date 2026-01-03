-- 1. sampling_reports → campaigns FK 제거
ALTER TABLE sampling_reports
DROP CONSTRAINT IF EXISTS fk_sampling_report_campaign;

ALTER TABLE sampling_reports
DROP CONSTRAINT IF EXISTS uq_sampling_report_campaign;

-- 2. campaign_id 컬럼 제거
ALTER TABLE sampling_reports
DROP COLUMN IF EXISTS campaign_id;

-- 1. FK 제거
ALTER TABLE campaigns
DROP CONSTRAINT IF EXISTS fk_campaign_company;

-- 2. company_id 제거
ALTER TABLE campaigns
DROP COLUMN IF EXISTS company_id;

-- 3. 기존 체크 제거
ALTER TABLE campaigns
DROP CONSTRAINT IF EXISTS campaigns_status_check;

UPDATE campaigns
SET status = 'Draft'
WHERE status NOT IN ('Draft', 'Submitted', 'Approved', 'Rejected', 'Completed')
   OR status IS NULL;

-- 5. 새 체크 제약 추가
ALTER TABLE campaigns
ADD CONSTRAINT campaigns_status_check
CHECK (
    status IN ('Draft', 'Submitted', 'Approved', 'Rejected', 'Completed')
);

-- 6. 기본값
ALTER TABLE campaigns
ALTER COLUMN status SET DEFAULT 'Draft';

-- 7. NOT NULL
ALTER TABLE campaigns
ALTER COLUMN status SET NOT NULL;

ALTER TABLE collaborations
DROP CONSTRAINT IF EXISTS collaborations_status_check;


-- 1) 기존 enum 타입 이름 변경
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_type WHERE typname = 'collaborationstatus') THEN
        ALTER TYPE collaborationstatus RENAME TO collaborationstatus_old;
    END IF;
END $$;


-- 2) 새 enum 타입 생성
CREATE TYPE collaborationstatus AS ENUM (
    'FindingCompany',
    'RecommendationReady',
    'WaitingCompanyResponse',
    'ContractSent',
    'WaitingStudentSignature',
    'WaitingAdminApproval',
    'WaitingReportUpload',
    'WaitingReportApproval',
    'Completed'
);


-- 3) collaboration.status 타입 변경 및 값 매핑
ALTER TABLE collaborations
    ALTER COLUMN status TYPE collaborationstatus
    USING (
        CASE status::text
            WHEN 'Ready' THEN 'FindingCompany'
            WHEN 'InProgress' THEN 'RecommendationReady'
            WHEN 'WaitingReceipt' THEN 'WaitingReportUpload'
            WHEN 'Completed' THEN 'Completed'
            ELSE 'FindingCompany'
        END
    )::collaborationstatus;


-- 4) 이전 enum 타입 제거
DROP TYPE IF EXISTS collaborationstatus_old;

ALTER TABLE matching_requests
ADD COLUMN selected_company_id BIGINT;

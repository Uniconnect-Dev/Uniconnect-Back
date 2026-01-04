-- business_registrations 테이블에 company_id 추가

-- 1. company_id 컬럼 추가
ALTER TABLE business_registrations
    ADD COLUMN IF NOT EXISTS company_id BIGINT;

-- 2. manager_email 컬럼 추가 (결제 송장 발송용)
ALTER TABLE business_registrations
    ADD COLUMN IF NOT EXISTS manager_email VARCHAR(120);

-- 3. 기존 데이터 마이그레이션 (User를 통해 Company 연결)
-- user 테이블에 company_id가 있다면 그것을 활용
UPDATE business_registrations
SET company_id = (
    SELECT u.company_id
    FROM users u
    WHERE u.user_id = business_registrations.user_id
)
WHERE company_id IS NULL AND user_id IS NOT NULL;

-- 4. 외래키 제약 추가
ALTER TABLE business_registrations
    ADD CONSTRAINT fk_business_registrations_company_id
        FOREIGN KEY (company_id) REFERENCES companies(company_id);

-- 5. 유니크 제약 추가 (회사당 1개의 사업자등록증)
ALTER TABLE business_registrations
    ADD CONSTRAINT uk_business_registrations_company_id
        UNIQUE(company_id);

-- 6. 인덱스 생성
CREATE INDEX IF NOT EXISTS idx_business_registrations_company_id
    ON business_registrations(company_id);

CREATE INDEX IF NOT EXISTS idx_business_registrations_registration_no
    ON business_registrations(registration_no);
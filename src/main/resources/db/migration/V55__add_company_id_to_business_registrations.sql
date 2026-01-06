-- V50__add_company_id_to_business_registrations.sql
-- 목적: business_registrations 테이블에 company 관계 추가 및 invoices 테이블 수정

-- ===== 1) business_registrations 테이블 수정 =====

-- 1-1) company_id 칼럼 추가
ALTER TABLE business_registrations
    ADD COLUMN IF NOT EXISTS company_id BIGINT NOT NULL;

-- 1-2) company_id에 대한 FK 생성
ALTER TABLE business_registrations
    ADD CONSTRAINT fk_business_registrations_company_id
        FOREIGN KEY (company_id) REFERENCES companies(company_id) ON DELETE CASCADE;

-- 1-3) 조회 성능 향상을 위한 인덱스
CREATE INDEX IF NOT EXISTS idx_business_registrations_company_id
    ON business_registrations(company_id);

CREATE INDEX IF NOT EXISTS idx_business_registrations_user_company
    ON business_registrations(user_id, company_id);
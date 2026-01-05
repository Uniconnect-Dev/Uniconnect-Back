-- V53__add_student_org_to_payments.sql
-- Payment 테이블에 student_org_id 컬럼 추가

-- 1. student_org_id 컬럼 추가
ALTER TABLE payments
    ADD COLUMN IF NOT EXISTS student_org_id BIGINT;

-- 2. 외래키 제약조건 추가
ALTER TABLE payments
    ADD CONSTRAINT fk_payments_student_org_id
        FOREIGN KEY (student_org_id)
            REFERENCES student_org(student_org_id)
            ON DELETE SET NULL;

-- 3. 인덱스 생성 (StudentOrg별 결제 조회 성능 향상)
CREATE INDEX IF NOT EXISTS idx_payments_student_org_id ON payments(student_org_id);

-- 4. company_id와 student_org_id 복합 인덱스 (기업 및 학생단체별 결제 조회)
CREATE INDEX IF NOT EXISTS idx_payments_company_student_org ON payments(company_id, student_org_id);
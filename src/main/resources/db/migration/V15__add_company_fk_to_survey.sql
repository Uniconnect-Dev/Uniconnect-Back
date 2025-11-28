-- 1) company_id 컬럼 추가 (null 허용)
ALTER TABLE survey
ADD COLUMN company_id BIGINT;

-- 2) company_id → companies(company_id) FK 생성
ALTER TABLE survey
ADD CONSTRAINT fk_survey_company
FOREIGN KEY (company_id)
REFERENCES companies(company_id)
ON DELETE SET NULL;

-- 3) student_org_id는 이미 존재한다고 가정 → null 허용으로 변경
ALTER TABLE survey
ALTER COLUMN student_org_id DROP NOT NULL;

-- 4) 기존 데이터 처리 (optional)
-- 이미 설문이 있다면 company_id를 임시로 null로 둔다
UPDATE survey
SET company_id = NULL
WHERE company_id IS NULL;

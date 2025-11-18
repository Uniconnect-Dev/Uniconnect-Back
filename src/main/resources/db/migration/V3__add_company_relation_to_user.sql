-- 1) company_id 컬럼 추가 (nullable 허용)
ALTER TABLE users
ADD COLUMN company_id BIGINT;

-- 2) 외래키 추가
ALTER TABLE users
ADD CONSTRAINT fk_users_company
FOREIGN KEY (company_id)
REFERENCES companies (company_id)
ON DELETE SET NULL;
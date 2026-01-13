
delete from companies where industry_id=1 or industry_id=2;
delete from industries where industry_id=1 or industry_id=2;

-- 1. 기존 type 컬럼 삭제
ALTER TABLE industries
DROP COLUMN IF EXISTS type;

-- 2. 새 type 컬럼 추가 (VARCHAR)
ALTER TABLE industries
    ADD COLUMN type VARCHAR(100);

-- 3) (선택) 앞으로는 반드시 값 넣게 하고 싶으면 default 제거
ALTER TABLE industries
    ALTER COLUMN type DROP DEFAULT;

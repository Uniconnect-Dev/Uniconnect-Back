-- 1. collaboration_id 컬럼 추가
ALTER TABLE contracts
ADD COLUMN collaboration_id BIGINT;

-- 2. FK 연결
ALTER TABLE contracts
ADD CONSTRAINT fk_contract_collaboration
FOREIGN KEY (collaboration_id)
REFERENCES collaborations(collaboration_id);

-- 3. (선택) 기존 데이터 마이그레이션
-- match_request_id → collaboration_id 매핑
UPDATE contracts c
SET collaboration_id = col.collaboration_id
FROM collaborations col
WHERE c.match_request_id = col.match_request_id;

-- 4. match_request_id 제거
ALTER TABLE contracts
DROP COLUMN match_request_id;
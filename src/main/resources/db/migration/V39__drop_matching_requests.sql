DROP TABLE IF EXISTS matching_requests CASCADE;

-- 1. contracts 테이블
ALTER TABLE contracts
    RENAME COLUMN matching_id TO match_request_id;

ALTER TABLE contracts
    DROP CONSTRAINT IF EXISTS fk_contract_matching;

ALTER TABLE contracts
    ADD CONSTRAINT fk_contract_match_request
    FOREIGN KEY (match_request_id)
    REFERENCES collaboration_match_requests(id)
    ON DELETE CASCADE;

-- 2. receipts 테이블
ALTER TABLE receipts
    RENAME COLUMN matching_id TO match_request_id;

ALTER TABLE receipts
    DROP CONSTRAINT IF EXISTS fk_receipt_matching;

ALTER TABLE receipts
    ADD CONSTRAINT fk_receipt_match_request
    FOREIGN KEY (match_request_id)
    REFERENCES collaboration_match_requests(id)
    ON DELETE CASCADE;

   -- 3. collaborations 테이블
   ALTER TABLE collaborations
       RENAME COLUMN matching_id TO match_request_id;

   ALTER TABLE collaborations
       DROP CONSTRAINT IF EXISTS fk_contract_matching;

   ALTER TABLE collaborations
       ADD CONSTRAINT fk_contract_match_request
       FOREIGN KEY (match_request_id)
       REFERENCES collaboration_match_requests(id)
       ON DELETE CASCADE;

   -- 4. matching_messages 테이블
ALTER TABLE matching_messages
   RENAME COLUMN matching_id TO match_request_id;

ALTER TABLE matching_messages
   DROP CONSTRAINT IF EXISTS fk_contract_matching;

ALTER TABLE matching_messages
   ADD CONSTRAINT fk_contract_match_request
   FOREIGN KEY (match_request_id)
   REFERENCES collaboration_match_requests(id)
   ON DELETE CASCADE;

   -- 1. sender 컬럼 추가
   ALTER TABLE collaboration_match_requests
   ADD COLUMN sender VARCHAR(20);

   -- 2. 기존 데이터 기본값 세팅 (기존은 학생단체 발송으로 가정)
   UPDATE collaboration_match_requests
   SET sender = 'STUDENT_ORG'
   WHERE sender IS NULL;

   -- 3. NOT NULL 제약
   ALTER TABLE collaboration_match_requests
   ALTER COLUMN sender SET NOT NULL;

   -- 4. CHECK 제약
   ALTER TABLE collaboration_match_requests
   ADD CONSTRAINT chk_match_sender
   CHECK (sender IN ('STUDENT_ORG', 'COMPANY'));

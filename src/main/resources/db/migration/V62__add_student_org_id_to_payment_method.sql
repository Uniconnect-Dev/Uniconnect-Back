-- PaymentMethod 테이블에 student_org_id FK 추가

ALTER TABLE payment_methods
    ADD COLUMN IF NOT EXISTS student_org_id BIGINT;

-- Foreign Key 제약 조건 추가
-- ALTER TABLE payment_methods
--     ADD CONSTRAINT IF NOT EXISTS fk_payment_methods_student_org_id
--     FOREIGN KEY (student_org_id) REFERENCES student_orgs(student_org_id) ON DELETE SET NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.table_constraints
        WHERE constraint_name = 'fk_payments_methods_student_org_id'
          AND table_name = 'payment_methods'
    ) THEN
ALTER TABLE payment_methods
    ADD CONSTRAINT fk_payments_methods_student_org_id FOREIGN KEY (student_org_id) REFERENCES student_orgs(student_org_id) ON DELETE SET NULL;
END IF;
END $$;

-- 인덱스 생성
CREATE INDEX IF NOT EXISTS idx_payment_methods_student_org_id
    ON payment_methods(student_org_id);

-- 로그
-- ✅ PaymentMethod 테이블에 student_org_id FK 추가 완료
-- Payment 테이블에 product_id FK 추가

ALTER TABLE payments
    ADD COLUMN IF NOT EXISTS product_id BIGINT;

-- Foreign Key 제약 조건 추가: cst not exists 안됨
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.table_constraints
        WHERE constraint_name = 'fk_payments_product_id'
          AND table_name = 'payments'
    ) THEN
ALTER TABLE payments
    ADD CONSTRAINT fk_payments_product_id
        FOREIGN KEY (product_id)
            REFERENCES products(product_id)
            ON DELETE SET NULL;
END IF;
END $$;

-- 인덱스 생성
CREATE INDEX IF NOT EXISTS idx_payments_product_id
    ON payments(product_id);

-- 로그
-- ✅ Payment 테이블에 product_id FK 추가 완료
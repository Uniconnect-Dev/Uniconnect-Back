-- Invoice 관련 테이블 정리 및 invoice_requests 최종 정의

-- 1. 기존 invoice 관련 테이블 확인 및 제거
DROP TABLE IF EXISTS invoices CASCADE;
DROP TABLE IF EXISTS invoice_details CASCADE;
DROP TABLE IF EXISTS invoice_items CASCADE;

-- 2. 기존 invoice_requests 테이블 확인
-- 만약 V57에서 생성한 invoice_requests가 있다면 스키마 업데이트, 없으면 생성
DROP TABLE IF EXISTS invoice_requests CASCADE;

-- 3. invoice_requests 테이블 신규 생성 (최종 버전)
CREATE TABLE invoice_requests (
                                  invoice_request_id BIGSERIAL PRIMARY KEY,

    -- === 결제 기본 정보 ===
                                  payment_id BIGINT NOT NULL UNIQUE,
                                  payment_date_time TIMESTAMP NOT NULL,
                                  payment_method VARCHAR(50) NOT NULL,

    -- === 고객 정보 ===
                                  customer_name VARCHAR(100) NOT NULL,
                                  email VARCHAR(100) NOT NULL,
                                  phone VARCHAR(20) NOT NULL,

    -- === 카드/계좌 정보 ===
                                  card_number VARCHAR(50),
                                  approval_number VARCHAR(100),

    -- === 금액 정보 ===
                                  original_amount BIGINT NOT NULL,
                                  discount_amount BIGINT NOT NULL DEFAULT 0,
                                  tax_amount BIGINT NOT NULL,
                                  partnership_fee BIGINT NOT NULL,
                                  additional_marketing BIGINT NOT NULL DEFAULT 0,
                                  total_amount BIGINT NOT NULL,
                                  net_amount BIGINT NOT NULL DEFAULT 0,

    -- === 사업자 정보 ===
                                  biz_number VARCHAR(50) NOT NULL,
                                  company_name VARCHAR(150) NOT NULL,
                                  representative_name VARCHAR(100) NOT NULL,
                                  address TEXT NOT NULL,
                                  biz_type VARCHAR(50) NOT NULL,
                                  biz_item VARCHAR(50) NOT NULL,

    -- === 파일 정보 ===
                                  biz_cert_url TEXT,
                                  contract_file_url TEXT,

    -- === 상태 ===
                                  status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
                                  rejection_reason TEXT,

    -- === 타임스탬프 ===
                                  approved_at TIMESTAMP,
                                  in_progress_at TIMESTAMP,
                                  completed_at TIMESTAMP,
                                  rejected_at TIMESTAMP,
                                  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- === 관계 정보 ===
                                  user_id BIGINT NOT NULL,
                                  admin_id BIGINT
);

-- 4. 인덱스 생성
CREATE INDEX idx_invoice_requests_user_id ON invoice_requests(user_id);
CREATE INDEX idx_invoice_requests_status ON invoice_requests(status);
CREATE INDEX idx_invoice_requests_payment_id ON invoice_requests(payment_id);
CREATE INDEX idx_invoice_requests_created_at ON invoice_requests(created_at DESC);

-- 5. Foreign Key 제약 조건 추가
ALTER TABLE invoice_requests
    ADD CONSTRAINT fk_invoice_requests_payment_id
        FOREIGN KEY (payment_id) REFERENCES payments(payment_id) ON DELETE CASCADE;

-- 로그
-- ✅ Invoice 관련 테이블 정리 완료
-- - 기존 invoices 테이블 제거
-- - 기존 invoice_details 테이블 제거
-- - invoice_requests 테이블 신규 생성 (최종 스키마)
-- - 필요한 인덱스 및 FK 제약 조건 추가
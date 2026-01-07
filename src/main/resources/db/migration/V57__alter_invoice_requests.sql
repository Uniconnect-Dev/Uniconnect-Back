-- InvoiceRequest 테이블 재구성
DROP TABLE IF EXISTS invoice_requests CASCADE;
DROP TABLE IF EXISTS invoice CASCADE;

CREATE TABLE invoice_requests (
                                  invoice_request_id BIGSERIAL PRIMARY KEY,

    -- 결제 기본 정보
                                  payment_id BIGINT NOT NULL,
                                  payment_date_time TIMESTAMP NOT NULL,
                                  payment_method VARCHAR(50) NOT NULL,

    -- 고객 정보
                                  customer_name VARCHAR(100) NOT NULL,
                                  email VARCHAR(100) NOT NULL,
                                  phone VARCHAR(20) NOT NULL,

    -- 카드/계좌 정보
                                  card_number VARCHAR(50),
                                  approval_number VARCHAR(100),

    -- 금액 정보
                                  original_amount BIGINT NOT NULL,
                                  discount_amount BIGINT NOT NULL DEFAULT 0,
                                  tax_amount BIGINT NOT NULL,
                                  partnership_fee BIGINT NOT NULL,
                                  additional_marketing BIGINT NOT NULL DEFAULT 0,
                                  total_amount BIGINT NOT NULL,
                                  net_amount BIGINT NOT NULL,

    -- 사업자 정보
                                  biz_number VARCHAR(50) NOT NULL,
                                  company_name VARCHAR(150) NOT NULL,
                                  representative_name VARCHAR(100) NOT NULL,
                                  address TEXT NOT NULL,
                                  biz_type VARCHAR(50) NOT NULL,
                                  biz_item VARCHAR(50) NOT NULL,

    -- 파일 정보
                                  biz_cert_url TEXT,
                                  contract_file_url TEXT,

    -- 상태
                                  status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
                                  user_id BIGINT NOT NULL,

    -- 타임스탬프
                                  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                                  UNIQUE(payment_id)
);

CREATE INDEX idx_invoice_requests_user_id ON invoice_requests(user_id);
CREATE INDEX idx_invoice_requests_status ON invoice_requests(status);
CREATE INDEX idx_invoice_requests_payment_id ON invoice_requests(payment_id);
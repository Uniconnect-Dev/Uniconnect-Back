CREATE TABLE invoice_requests (
    invoice_request_id BIGSERIAL PRIMARY KEY,

    -- 기본 정보
    event_name VARCHAR(255) NOT NULL,
    received_amount BIGINT NOT NULL,

    biz_cert_url TEXT NOT NULL,
    contract_file_url TEXT,

    -- 기업 사업자 정보
    biz_number VARCHAR(50) NOT NULL,
    company_name VARCHAR(255) NOT NULL,
    ceo_name VARCHAR(255) NOT NULL,
    address VARCHAR(500) NOT NULL,
    biz_type VARCHAR(255) NOT NULL,
    contact_email VARCHAR(255) NOT NULL,

    invoice_type VARCHAR(50) NOT NULL,
    company_contact_phone VARCHAR(50) NOT NULL,

    -- 상태
    status VARCHAR(50) NOT NULL,

    -- BaseEntity fields
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    is_deleted BOOLEAN DEFAULT FALSE
);

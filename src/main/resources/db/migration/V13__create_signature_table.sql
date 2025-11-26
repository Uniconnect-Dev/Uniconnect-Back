CREATE TABLE signature (
    signature_id BIGSERIAL PRIMARY KEY,

    user_id BIGINT NOT NULL,
    document_hash VARCHAR(255) NOT NULL,
    signature_hash VARCHAR(255) NOT NULL,
    timestamp BIGINT NOT NULL,

    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW()
);

-- 자주 조회하게 될 항목들 인덱스 생성
CREATE INDEX idx_signature_user_id
    ON signature(user_id);

CREATE INDEX idx_signature_document_hash
    ON signature(document_hash);
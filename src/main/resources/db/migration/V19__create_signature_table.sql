CREATE TABLE signatures (
    signature_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    signature_hash TEXT NOT NULL,
    timestamp BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT NOW()
);
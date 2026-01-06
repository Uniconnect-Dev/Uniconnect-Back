-- V54__add_email_verification_fields.sql
-- 목적: 기존 email_verifications 테이블에 이메일 인증 관련 필드 추가

-- 1) 코드 해시 및 만료 시간 칼럼 추가
ALTER TABLE email_verifications
    ADD COLUMN IF NOT EXISTS code_hash VARCHAR(255),
    ADD COLUMN IF NOT EXISTS expires_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS verified BOOLEAN DEFAULT false,
    ADD COLUMN IF NOT EXISTS attempt_count INTEGER DEFAULT 0,
    ADD COLUMN IF NOT EXISTS last_sent_at TIMESTAMP;

-- 2) email 칼럼을 UNIQUE로 설정 (중복 방지)
ALTER TABLE email_verifications
    ADD CONSTRAINT IF NOT EXISTS email_verifications_email_unique UNIQUE (email);

-- 3) created_at 칼럼 추가 (없을 경우)
ALTER TABLE email_verifications
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- 4) 조회 성능 향상을 위한 인덱스 생성
CREATE INDEX IF NOT EXISTS idx_email_verifications_email
    ON email_verifications(email);

CREATE INDEX IF NOT EXISTS idx_email_verifications_verified
    ON email_verifications(verified);

CREATE INDEX IF NOT EXISTS idx_email_verifications_expires_at
    ON email_verifications(expires_at);
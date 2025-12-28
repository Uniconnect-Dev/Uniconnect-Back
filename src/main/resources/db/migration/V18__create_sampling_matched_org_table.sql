-- =====================================
-- TABLE: sampling_matched_org
-- 학생단체 선택 저장 테이블
-- =====================================

CREATE TABLE sampling_matched_org (
    id BIGSERIAL PRIMARY KEY,

    sampling_request_id BIGINT NOT NULL,
    student_org_id BIGINT NOT NULL,

    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW()
);

-- =====================================
-- FOREIGN KEYS
-- =====================================

ALTER TABLE sampling_matched_org
    ADD CONSTRAINT fk_matched_org_sampling_request
        FOREIGN KEY (sampling_request_id)
        REFERENCES sampling_request (sampling_request_id)
        ON DELETE CASCADE;

ALTER TABLE sampling_matched_org
    ADD CONSTRAINT fk_matched_org_student_org
        FOREIGN KEY (student_org_id)
        REFERENCES student_orgs (student_org_id)
        ON DELETE CASCADE;

-- =====================================
-- INDEXES
-- =====================================

CREATE INDEX idx_matched_org_sampling_request
    ON sampling_matched_org (sampling_request_id);

CREATE INDEX idx_matched_org_student_org
    ON sampling_matched_org (student_org_id);

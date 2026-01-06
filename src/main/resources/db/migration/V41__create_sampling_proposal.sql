CREATE TABLE sampling_proposal (
    proposal_id BIGSERIAL PRIMARY KEY,

    -- 작성자 (기업 계정)
    company_user_id BIGINT NOT NULL,
    CONSTRAINT fk_sampling_proposal_company_user
        FOREIGN KEY (company_user_id)
        REFERENCES users(user_id)
        ON DELETE CASCADE,

    -- 1페이지: 기본 정보
    product_name VARCHAR(255),
    industry VARCHAR(50),
    sampling_purpose TEXT,
    sampling_start_date DATE,
    sampling_end_date DATE,
    product_count INTEGER,
    detail_request TEXT,
    proposal_file_url TEXT,

    -- 상태
    status VARCHAR(30) NOT NULL DEFAULT 'Draft',
    CONSTRAINT chk_sampling_proposal_status
        CHECK (status IN ('Draft', 'Submitted', 'Closed')),

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 인덱스
CREATE INDEX idx_sampling_proposal_company_user
    ON sampling_proposal(company_user_id);


-- sampling_request_id nullable로 변경
ALTER TABLE sampling_target_selection
ALTER COLUMN sampling_request_id DROP NOT NULL;

-- sampling_proposal_id 추가
ALTER TABLE sampling_target_selection
ADD COLUMN sampling_proposal_id BIGINT;

ALTER TABLE sampling_target_selection
ADD CONSTRAINT fk_sampling_target_selection_proposal
FOREIGN KEY (sampling_proposal_id)
REFERENCES sampling_proposal(proposal_id)
ON DELETE CASCADE;


DROP TABLE IF EXISTS collaboration_proposals CASCADE;

CREATE TABLE collaboration_proposals (
    proposal_id BIGSERIAL PRIMARY KEY,

    company_id BIGINT NOT NULL,
    student_org_id BIGINT NOT NULL,

    proposal_type VARCHAR(30) NOT NULL,

    contact_name VARCHAR(100) NOT NULL,
    contact_phone VARCHAR(20) NOT NULL,
    contact_email VARCHAR(120) NOT NULL,

    product_or_service_name VARCHAR(150) NOT NULL,
    industry VARCHAR(50),

    period_type VARCHAR(20) NOT NULL,
    start_date DATE,
    end_date DATE,

    proposal_content TEXT,
    attachment_url VARCHAR(500),

    collaboration_methods_json JSON NOT NULL,
    expected_outcomes_json JSON,

    agree_privacy BOOLEAN DEFAULT FALSE,
    agree_marketing BOOLEAN DEFAULT FALSE,

    status VARCHAR(20) NOT NULL,

    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_collab_proposal_company
        FOREIGN KEY (company_id)
        REFERENCES companies(company_id),

    CONSTRAINT fk_collab_proposal_student_org
        FOREIGN KEY (student_org_id)
        REFERENCES student_orgs(student_org_id)
);

ALTER TABLE collaboration_match_requests
ADD COLUMN sampling_proposal_id BIGINT,
ADD COLUMN collaboration_proposal_id BIGINT;

-- 기업 → 학생단체 (샘플링 제안)
ALTER TABLE collaboration_match_requests
ADD CONSTRAINT fk_match_sampling_proposal
FOREIGN KEY (sampling_proposal_id)
REFERENCES sampling_proposal(proposal_id)
ON DELETE CASCADE;

-- 기업 → 학생단체 (장기 협업 제안)
ALTER TABLE collaboration_match_requests
ADD CONSTRAINT fk_match_collaboration_proposal
FOREIGN KEY (collaboration_proposal_id)
REFERENCES collaboration_proposals (proposal_id)
ON DELETE CASCADE;

-- =========================
-- PartnershipType 제약조건
-- =========================
ALTER TABLE collaboration_proposals
ADD CONSTRAINT chk_collaboration_proposals_proposal_type
CHECK (proposal_type IN (
    'Discount',
    'Etc'
));

-- =========================
-- CollaborationPeriodType 제약조건
-- =========================
ALTER TABLE collaboration_proposals
ADD CONSTRAINT chk_collaboration_proposals_period_type
CHECK (period_type IN (
    'Always',
    'Fixed'
));

-- =========================
-- ProposalStatus 제약조건
-- =========================
ALTER TABLE collaboration_proposals
ADD CONSTRAINT chk_collaboration_proposals_status
CHECK (status IN (
    'Draft',
    'Submitted',
    'Approved',
    'Rejected'
));

-- company_id nullable 보장
ALTER TABLE collaboration_match_requests
    ALTER COLUMN company_id DROP NOT NULL;

-- student_org_id nullable 보장
ALTER TABLE collaboration_match_requests
    ALTER COLUMN student_org_id DROP NOT NULL;


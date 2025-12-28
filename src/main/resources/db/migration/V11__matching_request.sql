CREATE TABLE collaboration_match_requests (
    matching_request_id BIGSERIAL PRIMARY KEY,
    student_org_id BIGINT NOT NULL,
    company_id BIGINT NOT NULL,
    event_title VARCHAR(255),
    desired_date DATE,
    industry VARCHAR(255),
    collaboration_type VARCHAR(255),
    status VARCHAR(50),
    requested_at TIMESTAMP,
    responded_at TIMESTAMP
);

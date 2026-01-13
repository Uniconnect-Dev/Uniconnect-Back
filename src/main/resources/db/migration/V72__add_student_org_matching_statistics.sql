-- 학생단체-기업 매칭 관리 통계 테이블
CREATE TABLE student_org_matching_statistics (
                                                 id BIGSERIAL PRIMARY KEY,
                                                 student_org_id BIGINT NOT NULL UNIQUE,
                                                 total_matchings INTEGER DEFAULT 0,
                                                 completed_matchings INTEGER DEFAULT 0,
                                                 pending_matchings INTEGER DEFAULT 0,
                                                 failed_matchings INTEGER DEFAULT 0,
                                                 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                                 updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                                 CONSTRAINT fk_student_org_id FOREIGN KEY (student_org_id) REFERENCES student_orgs(student_org_id) ON DELETE CASCADE
);

-- 인덱스 추가
CREATE INDEX idx_student_org_matching_statistics_student_org_id
    ON student_org_matching_statistics(student_org_id);
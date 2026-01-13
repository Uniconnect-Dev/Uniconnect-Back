
-- 기업-학생단체 매칭 관리 통계 테이블
CREATE TABLE company_matching_statistics (
                                             id BIGSERIAL PRIMARY KEY,
                                             company_id BIGINT NOT NULL UNIQUE,
                                             total_matchings INTEGER DEFAULT 0,
                                             completed_matchings INTEGER DEFAULT 0,
                                             pending_matchings INTEGER DEFAULT 0,
                                             failed_matchings INTEGER DEFAULT 0,
                                             created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                             updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                             CONSTRAINT fk_company_id FOREIGN KEY (company_id) REFERENCES companies(company_id) ON DELETE CASCADE
);

-- 인덱스 추가
CREATE INDEX idx_company_matching_statistics_company_id
    ON company_matching_statistics(company_id);
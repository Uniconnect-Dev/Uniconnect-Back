
-- 업태 테이블 생성
CREATE TABLE business_types (
                                business_type_id BIGSERIAL PRIMARY KEY,
                                name VARCHAR(100) NOT NULL UNIQUE,
                                description TEXT,
                                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 업태 데이터 삽입
INSERT INTO business_types (name, description) VALUES
                                                   ('법인사업자', '법인으로 등록된 사업자'),
                                                   ('개인사업자', '개인으로 등록된 사업자'),
                                                   ('스타트업', '초기 단계 스타트업'),
                                                   ('중소기업', '중소기업 규모'),
                                                   ('중견기업', '중견기업 규모'),
                                                   ('대기업', '대기업 규모'),
                                                   ('계열사', '대기업의 계열사'),
                                                   ('B2B', 'Business to Business 모델'),
                                                   ('B2C', 'Business to Consumer 모델'),
                                                   ('B2B2C', 'Business to Business to Consumer 모델'),
                                                   ('SaaS', 'Software as a Service'),
                                                   ('플랫폼 기반', '플랫폼 기반 비즈니스'),
                                                   ('구독형 서비스', '구독형 서비스 모델'),
                                                   ('프로젝트 기반', '프로젝트 기반 비즈니스'),
                                                   ('자체 개발', '자체 개발 제품/서비스'),
                                                   ('외주 개발', '외주 개발 서비스'),
                                                   ('운영 대행', '운영 대행 서비스'),
                                                   ('위탁 운영', '위탁 운영 서비스'),
                                                   ('솔루션 제공', '솔루션 제공 업체'),
                                                   ('API 제공', 'API 기반 서비스'),
                                                   ('온라인 서비스', '온라인 기반 서비스'),
                                                   ('오프라인 운영', '오프라인 기반 운영'),
                                                   ('온·오프라인 병행', '온라인과 오프라인 병행'),
                                                   ('직접 판매', '직접 판매 모델'),
                                                   ('간접 판매', '간접 판매 모델'),
                                                   ('파트너십 기반', '파트너십 기반 비즈니스'),
                                                   ('기술 기반 기업', '기술 기반 기업'),
                                                   ('데이터 기반 기업', '데이터 기반 기업'),
                                                   ('플랫폼 기업', '플랫폼 기업'),
                                                   ('콘텐츠 기업', '콘텐츠 기반 기업'),
                                                   ('연구 중심 기업', '연구 중심 기업');

-- 인덱스
CREATE INDEX idx_business_types_name ON business_types(name);

-- companies 테이블에 business_type_id 컬럼 추가
ALTER TABLE companies
    ADD COLUMN business_type_id BIGINT,
ADD CONSTRAINT fk_companies_business_type_id
    FOREIGN KEY (business_type_id) REFERENCES business_types(business_type_id) ON DELETE SET NULL;

-- 인덱스
CREATE INDEX idx_companies_business_type_id ON companies(business_type_id);
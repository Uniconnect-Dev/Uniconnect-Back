-- industries 테이블 리팩토링
-- 1. type 컬럼 추가 (Enum 값 저장)
ALTER TABLE industries
    ADD COLUMN type VARCHAR(100);

-- 2. name 컬럼 추가 (한글명 저장)
ALTER TABLE industries
    ADD COLUMN name VARCHAR(100);

-- 3. 기존 industry_name 컬럼 삭제 (있는 경우)
DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'industries'
        AND column_name = 'industry_name'
    ) THEN
ALTER TABLE industries DROP COLUMN industry_name;
END IF;
END $$;

-- 4. type 컬럼에 UNIQUE 제약 조건 추가
ALTER TABLE industries
    ADD CONSTRAINT uk_industries_type UNIQUE (type);

-- 5. 인덱스 생성
CREATE INDEX if not exists idx_industries_type ON industries(type);
CREATE INDEX if not exists idx_industries_name ON industries(name);


-- business_types 테이블 리팩토링
-- 1. type 컬럼 추가 (Enum 값 저장)
ALTER TABLE business_types
    ADD COLUMN type VARCHAR(100);

-- 2. name 컬럼이 없으면 추가 (있으면 스킵)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'business_types'
        AND column_name = 'name'
    ) THEN
ALTER TABLE business_types ADD COLUMN name VARCHAR(100);
END IF;
END $$;

-- 3. type 컬럼에 UNIQUE 제약 조건 추가
ALTER TABLE business_types
    ADD CONSTRAINT uk_business_types_type UNIQUE (type);

-- 4. 인덱스 생성
CREATE INDEX if not exists idx_business_types_type ON business_types(type);
CREATE INDEX if not exists idx_business_types_name ON business_types(name);

-- 업종(Industry) Enum 데이터 삽입
INSERT INTO industries (type, name, description) VALUES
                                                     ('SOFTWARE_DEVELOPMENT', '소프트웨어 개발업', 'Software Development'),
                                                     ('IT_SERVICE', 'IT 서비스업', 'IT Service'),
                                                     ('INFORMATION_COMMUNICATION', '정보통신업', 'Information & Communication'),
                                                     ('DATA_PROCESSING', '데이터 처리업', 'Data Processing'),
                                                     ('AI_SERVICE', '인공지능 서비스업', 'AI Service'),
                                                     ('CLOUD_SERVICE', '클라우드 서비스업', 'Cloud Service'),
                                                     ('PLATFORM_OPERATION', '플랫폼 운영업', 'Platform Operation'),
                                                     ('SYSTEM_INTEGRATION', '시스템 통합(SI)', 'System Integration'),
                                                     ('SOLUTION_DEVELOPMENT', '솔루션 개발업', 'Solution Development'),
                                                     ('CONSULTING', '컨설팅업', 'Consulting'),
                                                     ('MANAGEMENT_CONSULTING', '경영컨설팅업', 'Management Consulting'),
                                                     ('STRATEGY_CONSULTING', '전략컨설팅업', 'Strategy Consulting'),
                                                     ('MARKETING_CONSULTING', '마케팅 컨설팅업', 'Marketing Consulting'),
                                                     ('LEGAL_SERVICE', '법률 서비스업', 'Legal Service'),
                                                     ('ACCOUNTING_TAX_SERVICE', '회계·세무 서비스업', 'Accounting & Tax Service'),
                                                     ('HR_LABOR_SERVICE', '인사·노무 서비스업', 'HR & Labor Service'),
                                                     ('RESEARCH_SURVEY', '리서치·조사업', 'Research & Survey'),
                                                     ('ADVERTISING_AGENCY', '광고대행업', 'Advertising Agency'),
                                                     ('MARKETING_AGENCY', '마케팅대행업', 'Marketing Agency'),
                                                     ('DIGITAL_MARKETING', '디지털마케팅업', 'Digital Marketing'),
                                                     ('CONTENT_PRODUCTION', '콘텐츠 제작업', 'Content Production'),
                                                     ('MEDIA_CONTENT', '미디어 콘텐츠업', 'Media Content'),
                                                     ('VIDEO_PRODUCTION', '영상 제작업', 'Video Production'),
                                                     ('DESIGN_SERVICE', '디자인 서비스업', 'Design Service'),
                                                     ('BRAND_CONSULTING', '브랜드 컨설팅업', 'Brand Consulting'),
                                                     ('SERVICE', '서비스업', 'Service'),
                                                     ('OPERATION_AGENCY', '운영대행업', 'Operation Agency'),
                                                     ('OUTSOURCING', '아웃소싱업', 'Outsourcing'),
                                                     ('CRM_SERVICE', 'CRM 서비스업', 'CRM Service'),
                                                     ('MANUFACTURING', '제조업', 'Manufacturing'),
                                                     ('RESEARCH_DEVELOPMENT', '연구·개발(R&D)업', 'Research & Development'),
                                                     ('TECHNOLOGY_DEVELOPMENT', '기술 개발업', 'Technology Development'),
                                                     ('WHOLESALE_RETAIL', '도소매업', 'Wholesale & Retail'),
                                                     ('DISTRIBUTION', '유통업', 'Distribution'),
                                                     ('TRADE', '무역업', 'Trade'),
                                                     ('E_COMMERCE', '전자상거래업', 'E-Commerce'),
                                                     ('EDUCATION_SERVICE', '교육 서비스업', 'Education Service'),
                                                     ('CORPORATE_EDUCATION', '기업교육', 'Corporate Education'),
                                                     ('ONLINE_EDUCATION', '온라인 교육업', 'Online Education'),
                                                     ('HR_SERVICE', 'HR 서비스업', 'HR Service'),
                                                     ('RECRUITMENT_PLATFORM', '채용 플랫폼 운영업', 'Recruitment Platform'),
                                                     ('FINANCIAL_SERVICE', '금융 서비스업', 'Financial Service'),
                                                     ('FINTECH_SERVICE', '핀테크 서비스업', 'FinTech Service'),
                                                     ('PAYMENT_SERVICE', '결제 서비스업', 'Payment Service'),
                                                     ('DATA_FINANCE', '데이터 금융업', 'Data Finance')
    ON CONFLICT (type) DO NOTHING;

package com.uniConnect.company.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum IndustryType {

    SOFTWARE_DEVELOPMENT("소프트웨어 개발업"),
    IT_SERVICE("IT 서비스업"),
    INFORMATION_COMMUNICATION("정보통신업"),
    DATA_PROCESSING("데이터 처리업"),
    AI_SERVICE("인공지능 서비스업"),
    CLOUD_SERVICE("클라우드 서비스업"),
    PLATFORM_OPERATION("플랫폼 운영업"),
    SYSTEM_INTEGRATION("시스템 통합(SI)"),
    SOLUTION_DEVELOPMENT("솔루션 개발업"),
    CONSULTING("컨설팅업"),
    MANAGEMENT_CONSULTING("경영컨설팅업"),
    STRATEGY_CONSULTING("전략컨설팅업"),
    MARKETING_CONSULTING("마케팅 컨설팅업"),
    LEGAL_SERVICE("법률 서비스업"),
    ACCOUNTING_TAX_SERVICE("회계·세무 서비스업"),
    HR_LABOR_SERVICE("인사·노무 서비스업"),
    RESEARCH_SURVEY("리서치·조사업"),
    ADVERTISING_AGENCY("광고대행업"),
    MARKETING_AGENCY("마케팅대행업"),
    DIGITAL_MARKETING("디지털마케팅업"),
    CONTENT_PRODUCTION("콘텐츠 제작업"),
    MEDIA_CONTENT("미디어 콘텐츠업"),
    VIDEO_PRODUCTION("영상 제작업"),
    DESIGN_SERVICE("디자인 서비스업"),
    BRAND_CONSULTING("브랜드 컨설팅업"),
    SERVICE("서비스업"),
    OPERATION_AGENCY("운영대행업"),
    OUTSOURCING("아웃소싱업"),
    CRM_SERVICE("CRM 서비스업"),
    MANUFACTURING("제조업"),
    RESEARCH_DEVELOPMENT("연구·개발(R&D)업"),
    TECHNOLOGY_DEVELOPMENT("기술 개발업"),
    WHOLESALE_RETAIL("도소매업"),
    DISTRIBUTION("유통업"),
    TRADE("무역업"),
    E_COMMERCE("전자상거래업"),
    EDUCATION_SERVICE("교육 서비스업"),
    CORPORATE_EDUCATION("기업교육"),
    ONLINE_EDUCATION("온라인 교육업"),
    HR_SERVICE("HR 서비스업"),
    RECRUITMENT_PLATFORM("채용 플랫폼 운영업"),
    FINANCIAL_SERVICE("금융 서비스업"),
    FINTECH_SERVICE("핀테크 서비스업"),
    PAYMENT_SERVICE("결제 서비스업"),
    DATA_FINANCE("데이터 금융업"),
    FNB("F&B"),
    BEAUTY("뷰티");

    private final String displayName;

    public static IndustryType fromDisplayName(String displayName) {
        for (IndustryType type : values()) {
            if (type.displayName.equals(displayName)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid industry type: " + displayName);
    }
}
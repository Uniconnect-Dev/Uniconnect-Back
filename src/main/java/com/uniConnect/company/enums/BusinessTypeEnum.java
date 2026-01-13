package com.uniConnect.company.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BusinessTypeEnum {

    CORPORATION("법인사업자"),
    INDIVIDUAL("개인사업자"),
    STARTUP("스타트업"),
    SMALL_BUSINESS("중소기업"),
    MEDIUM_BUSINESS("중견기업"),
    LARGE_ENTERPRISE("대기업"),
    SUBSIDIARY("계열사"),
    B2B("B2B"),
    B2C("B2C"),
    B2B2C("B2B2C"),
    SAAS("SaaS"),
    PLATFORM_BASED("플랫폼 기반"),
    SUBSCRIPTION_SERVICE("구독형 서비스"),
    PROJECT_BASED("프로젝트 기반"),
    IN_HOUSE_DEVELOPMENT("자체 개발"),
    OUTSOURCED_DEVELOPMENT("외주 개발"),
    OPERATION_AGENCY("운영 대행"),
    CONSIGNMENT_OPERATION("위탁 운영"),
    SOLUTION_PROVIDER("솔루션 제공"),
    API_PROVIDER("API 제공"),
    ONLINE_SERVICE("온라인 서비스"),
    OFFLINE_OPERATION("오프라인 운영"),
    OMNI_CHANNEL("온·오프라인 병행"),
    DIRECT_SALES("직접 판매"),
    INDIRECT_SALES("간접 판매"),
    PARTNERSHIP_BASED("파트너십 기반"),
    TECH_BASED_COMPANY("기술 기반 기업"),
    DATA_BASED_COMPANY("데이터 기반 기업"),
    PLATFORM_COMPANY("플랫폼 기업"),
    CONTENT_COMPANY("콘텐츠 기업"),
    RESEARCH_FOCUSED_COMPANY("연구 중심 기업");

    private final String displayName;

    public static BusinessTypeEnum fromDisplayName(String displayName) {
        for (BusinessTypeEnum type : values()) {
            if (type.displayName.equals(displayName)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid business type: " + displayName);
    }
}
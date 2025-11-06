package com.uniConnect.compliance.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AgreementType {
    PROCESS_INFO(
            "이후 프로세스 안내",
            "학생 단체가 승인할 경우 매칭이 확정되며, 이후 계약서와 제품 설문지가 활성화될 예정입니다. " +
                    "학생 단체가 거절할 경우 샘플링이 어렵습니다."
    ),
    OFFPLATFORM_PENALTY(
            "플랫폼 이탈 협업 적발 시 불이익 안내",
            "UNI:CONNECT는 기업과 학생단체 간의 안전하고 투명한 협업을 보장하기 위해 모든 거래 과정을 플랫폼 내에서 관리합니다. " +
                    "플랫폼 외부에서의 직거래·협업이 적발될 경우, 서비스 이용 제한 및 향후 캠페인 참여 불가 등의 불이익이 발생할 수 있습니다."
    ),
    TERMS_ACK(
            "이용 약관 확인 안내",
            "이용약관에 대한 사용자의 인지 및 이해는 전적으로 사용자 본인의 책임이며, UNI:CONNECT는 이로 인한 문제에 책임을 지지 않습니다."
    );

    private final String title;
    private final String description;
}
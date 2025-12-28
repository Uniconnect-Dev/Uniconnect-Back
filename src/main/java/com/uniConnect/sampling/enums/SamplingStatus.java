package com.uniConnect.sampling.enums;

public enum SamplingStatus {
    // 요청 단계
    Draft,           // 학생단체가 초안 생성
    Submitted,       // 요청서 제출 (관리자 검토 대기)

    MatchingRequested,   // 학생단체가 매칭 요청 제출

    Approved,        // 요청 승인 (계약 진행 가능)
    Rejected,        // 요청 반려

    // 협업 진행 단계
    ContractPending,         // 전자 계약서 서명 대기 (학생단체)
    ContractApprovalPending, // 학생 서명 완료, 관리자 승인 대기
    ReceiptPending,          // 제품 수령 후 인수증 업로드 대기
    ReceiptApprovalPending,  // 인수증 승인 대기
    ReportPending,           // 행사 리포트 업로드 대기
    ReportApprovalPending,   // 리포트 승인 대기
    SurveyPending,           // 기업 설문 대기
    Completed                // 모든 절차 완료
}

package com.uniConnect.collaboration.enums;

public enum CollaborationStatus {
    ContractPending,         // Step1-1: 전자 계약서 서명 대기
    ContractApprovalPending, // Step1-2: 유니커넥트 최종 승인 대기
    ReceiptPending,          // Step2-1: 인수증 업로드 대기
    ReceiptApprovalPending,  // Step2-2: 인수증 승인 대기
    ReportPending,           // Step3-1: 리포트 업로드 대기
    ReportApprovalPending,   // Step3-2: 리포트 승인 대기
    SurveyPending,           // Step4: 설문 작성 대기
    Completed                // 모든 절차 완료
}

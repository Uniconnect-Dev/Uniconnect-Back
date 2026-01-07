package com.uniConnect.collaboration.enums;

public enum CollaborationStatus {

    // 계약
    ContractSent,              // 기업이 승인하여 계약서 발송됨
    WaitingStudentSignature,   // 학생 서명 대기
    WaitingAdminApproval,      // 학생 서명 완료 → Admin 승인 대기

    // 샘플링 전용
    WaitingReceiptUpload,      // 인수증 업로드 대기
    WaitingReceiptApproval,    // 인수증 어드민 승인 대기

    // 공통
    WaitingReportUpload,       // 리포트 파일 업로드 대기
    WaitingReportApproval,     // 리포트 제출 후 승인 대기

    Completed                  // 제휴 전체 완료
}

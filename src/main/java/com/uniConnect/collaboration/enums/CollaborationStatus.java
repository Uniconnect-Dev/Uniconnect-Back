package com.uniConnect.collaboration.enums;

public enum CollaborationStatus {

    ContractSent,              // 기업이 승인하여 계약서 발송됨
    WaitingStudentSignature,   // 학생 서명 대기
    WaitingAdminApproval,      // 학생 서명 완료 → Admin 승인 대기

    WaitingReportUpload,       // 리포트 파일 업로드 대기
    WaitingReportApproval,     // 리포트 제출 후 승인 대기

    Completed                  // 제휴 전체 완료
}

package com.uniConnect.collaboration.enums;

public enum CollaborationStatus {

    // Step 1: 제안서 접수 & 기업 탐색 중
    FindingCompany,            // 기업 탐색 중

    // Step 2: 추천 기업 확인 및 최종 요청
    RecommendationReady,       // 추천 기업 도착
    WaitingCompanyResponse,    // 학생이 최종 매칭 요청 → 기업 응답 대기

    // Step 3: 계약 진행
    ContractSent,              // 기업이 승인하여 계약서 발송됨
    WaitingStudentSignature,   // 학생 서명 대기
    WaitingAdminApproval,      // 학생 서명 완료 → Admin 승인 대기

    // Step 4: 마케팅 활동 보고
    WaitingReportUpload,       // 리포트 파일 업로드 대기
    WaitingReportApproval,     // 리포트 제출 후 승인 대기

    // 완료
    Completed                  // 제휴 전체 완료
}

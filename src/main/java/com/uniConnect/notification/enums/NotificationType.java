package com.uniConnect.notification.enums;

public enum NotificationType {
    ReportSubmitted,     // 리포트 제출
    ReportApproved,      // 리포트 승인
    CollaborationUpdate, // 협업 상태 변경
    ContractSigned,      // 계약 체결
    SamplingRequest,     // 샘플링 요청 도착
    SystemAlert,          // 시스템 알림

    // 결제 관련
    PaymentSuccess,        // 결제 성공
    PaymentFailed,         // 결제 실패
    RefundRequested,       // 환불 요청
    RefundCompleted       // 환불 완료
}
package com.uniConnect.payment.enums;

public enum RefundStatus {
    PENDING,       // 환불 대기중
    REVIEWING,     // 검토중
    APPROVED,      // 승인됨
    PROCESSING,    // 처리중
    COMPLETED,     // 완료됨
    REJECTED       // 거절됨
}

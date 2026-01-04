package com.uniConnect.payment.enums;

public enum PaymentStatus {
    PENDING,        // 결제 대기중
    PROCESSING,     // 결제 처리중
    SUCCESS,        // 결제 완료
    FAILED,         // 결제 실패
    CANCELED,       // 결제 취소
    REFUNDING,      // 환불 진행중
    REFUNDED        // 환불 완료
}

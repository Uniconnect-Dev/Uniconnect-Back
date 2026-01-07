package com.uniConnect.payment.enums;

public enum InvoiceStatus {
    PENDING, //자동 생성된 invoice(최초)
    APPROVED, //관리자 승인
    IN_PROGRESS, //국세청 처리중
    COMPLETED, //최종 발행 완료
    REJECTED //어느 단계든 거절가능
}
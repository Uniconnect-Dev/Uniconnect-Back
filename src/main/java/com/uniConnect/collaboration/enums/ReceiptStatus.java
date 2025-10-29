package com.uniConnect.collaboration.enums;

public enum ReceiptStatus {
    PendingUpload,     // 아직 학생단체가 인수증 안 넣음
    WaitingApproval,   // 학생단체 제출 완료 → 기업 서명 대기
    Approved           // 기업 서명까지 끝
}

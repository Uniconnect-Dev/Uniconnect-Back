package com.uniConnect.contract.enums;

public enum ContractStatus {
    PendingSignature,  // 아직 누구도 안 했음
    StudentSigned,      // 학생 단체 서명 완료, 회사 대기
    Signed,              // (계약서 기준) 양측 서명 완료
    ReceiptPending,     // 인수증 서명 대기
    ReceiptSigned       // 인수증까지 서명 완료, 전체 종료

}

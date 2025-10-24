package com.uniConnect.contract.enums;

public enum ContractStatus {
    PENDING_SIGNATURE,   // 아직 누구도 안 했음
    STUDENT_SIGNED,      // 학생 단체 서명 완료, 회사 대기
    SIGNED,              // (계약서 기준) 양측 서명 완료
    RECEIPT_PENDING,     // 인수증 서명 대기
    RECEIPT_SIGNED       // 인수증까지 서명 완료, 전체 종료

}

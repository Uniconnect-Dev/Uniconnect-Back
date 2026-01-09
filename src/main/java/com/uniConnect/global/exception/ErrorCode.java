package com.uniConnect.global.exception;

import org.springframework.http.HttpStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
public enum ErrorCode {

    // 인증 관련
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),


    // 요청 관련
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "잘못된 입력 값입니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "허용되지 않은 요청 방식입니다."),
    RESTAPI_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "허용되지 않은 외부 api입니다."),

    // 계약서 관련
    CONTRACT_NOT_FOUND(HttpStatus.NOT_FOUND, "계약서를 찾을 수 없습니다."),
    CONTRACT_ALREADY_SIGNED(HttpStatus.BAD_REQUEST, "이미 서명된 계약서입니다."),
    CONTRACT_NOT_APPROVED(HttpStatus.BAD_REQUEST, "관리자가 아직 승인하지 않은 계약입니다."),
    CONTRACT_NOT_SIGNED_YET(HttpStatus.BAD_REQUEST, "계약서 서명이 먼저 필요합니다."),
    RECEIPT_ALREADY_SIGNED(HttpStatus.BAD_REQUEST, "이미 인수증 서명이 완료되었습니다."),
    INVALID_RECEIPT_STATUS(HttpStatus.BAD_REQUEST, "현재 상태에서는 인수증 서명을 할 수 없습니다."),
    COMPANY_SIGNATURE_REQUIRED(HttpStatus.BAD_REQUEST, "기업 서명이 먼저 필요합니다."),
    INVALID_CONTRACT_STATUS(HttpStatus.BAD_REQUEST, "계약 상태가 올바르지 않습니다."),


    // 리소스 관련
    ENTITY_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 데이터입니다."),
    DUPLICATE_RESOURCE(HttpStatus.CONFLICT, "이미 존재하는 데이터입니다."),

    // 프로필 관련
    PROFILE_NOT_FOUND(HttpStatus.NOT_FOUND, "프로필을 찾을 수 없습니다."),
    ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 프로필이 존재합니다."),

    // 산업 관련
    INDUSTRY_NOT_FOUND(HttpStatus.NOT_FOUND, "산업 분류를 찾을 수 없습니다."),

    // Compliance 관련
    COMPLIANCE_AGREEMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "동의 내역을 찾을 수 없습니다."),
    COMPLIANCE_ALREADY_AGREED(HttpStatus.CONFLICT, "이미 동의한 요청입니다."),
    COMPLIANCE_INCOMPLETE(HttpStatus.BAD_REQUEST, "모든 항목에 동의해야 합니다."),

    // 서버 관련
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),

    // 샘플링 관련
    SAMPLING_REQUEST_NOT_FOUND(HttpStatus.NOT_FOUND, "샘플링 요청을 찾을 수 없습니다."),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 리소스를 찾을 수 없습니다."),
    REQUEST_NOT_FOUND(HttpStatus.NOT_FOUND, "샘플링 요청을 찾을 수 없습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "리소스를 찾을 수 없습니다."),

    // 리포트 관련
    INVALID_CAMPAIGN_STATE(HttpStatus.BAD_REQUEST, "캠페인이 리포트 업로드 대기 상태가 아닙니다."),
    COMPANY_NOT_FOUND(HttpStatus.NOT_FOUND, "기업 정보를 찾을 수 없습니다."),

    // 인증/인가 관련
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),

    // 협업 관련
    COLLABORATION_NOT_FOUND(HttpStatus.NOT_FOUND, "협업 정보를 찾을 수 없습니다."),
    CAMPAIGN_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 캠페인을 찾을 수 없습니다."),
    REPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 리포트를 찾을 수 없습니다."),
    INVALID_STATE(HttpStatus.BAD_REQUEST, "아직 기업이 확정되지 않은 협업입니다."),

    // 전자서명 관련
    SIGNATURE_EXPIRED(HttpStatus.BAD_REQUEST, "서명 시간이 만료되었습니다."),
    INVALID_SIGNATURE(HttpStatus.BAD_REQUEST, "서명 검증에 실패했습니다."),

    // payment 관련
    INVALID_AMOUNT(HttpStatus.BAD_REQUEST, "금액이 일치하지 않습니다."),
    PAYMENT_ALREADY_PROCESSED(HttpStatus.BAD_REQUEST, "이미 결제된 금액입니다."),
    PAYMENT_FAILED(HttpStatus.BAD_REQUEST, "결제에 실패했습니다."),
    INVALID_PAYMENT_STATUS(HttpStatus.BAD_REQUEST, "취소 불가능한 결제상태입니다"),

    // 세금계산서 관련
    INVALID_INVOICE_TYPE(HttpStatus.BAD_REQUEST, "잘못된 세금계산서 발행 방식입니다."),
    BUSINESS_NOT_VERIFIED(HttpStatus.BAD_REQUEST, "사업자 검증에 실패하였습니다"),

    // 환불 관련
    REFUND_FAILED(HttpStatus.BAD_REQUEST, "PG사 환불 처리에 실패하였습니다"),

    // 암,복호화 관련
    ENCRYPTION_FAILED(HttpStatus.BAD_REQUEST, "암호화에 실패하였습니다."),
    DECRYPTION_FAILED(HttpStatus.BAD_REQUEST, "복호화에 실패하였습니다.");


    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}

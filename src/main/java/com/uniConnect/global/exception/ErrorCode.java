package com.uniConnect.global.exception;

import org.springframework.http.HttpStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 인증 관련
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),

    // 요청 관련
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "잘못된 입력 값입니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "허용되지 않은 요청 방식입니다."),

    // 계약서 관련
    CONTRACT_NOT_FOUND(HttpStatus.NOT_FOUND, "계약서를 찾을 수 없습니다."),
    CONTRACT_ALREADY_SIGNED(HttpStatus.BAD_REQUEST, "이미 서명된 계약서입니다."),

    // 리소스 관련
    ENTITY_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 데이터입니다."),
    DUPLICATE_RESOURCE(HttpStatus.CONFLICT, "이미 존재하는 데이터입니다."),

    // 사용자 관련
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),

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
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String message;
}

package com.uniConnect.global.exception;

import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@Builder
public class ErrorResponse {
    private final int status;
    private final String error;
    private final String message;

    public static ErrorResponse of(ErrorCode errorCode) {
        return ErrorResponse.builder()
                .status(errorCode.getStatus().value())
                .error(errorCode.getStatus().name())
                .message(errorCode.getMessage())
                .build();
    }

    public static ErrorResponse of(HttpStatus status, String message) {
        return ErrorResponse.builder()
                .status(status.value())
                .error(status.name())
                .message(message)
                .build();
    }
}

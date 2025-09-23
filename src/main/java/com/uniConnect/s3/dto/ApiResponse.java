package com.uniConnect.s3.dto;

public record ApiResponse<T>(
        String code,      // "OK" | "ERROR"
        String message,   // 사용자 친화적 메시지
        T data            // 성공시 payload
) {
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>("OK", "", data);
    }
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>("ERROR", message, null);
    }
}

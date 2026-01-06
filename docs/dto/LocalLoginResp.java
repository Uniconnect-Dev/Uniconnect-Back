package com.uniConnect.member.security.local.dto;

import com.uniConnect.member.enums.UserRole;
import com.uniConnect.member.enums.UserStatus;

import java.time.Instant;

public record LocalLoginResp(
        String tokenType,              // "Bearer"
        String accessToken,
        long   accessTokenExpiresIn,   // 초 단위
        String refreshToken,           // (옵션) 쿠키로 줄 거면 null 가능
        long   refreshTokenExpiresIn,  // (옵션) 초 단위

        Long userId,
        String username,               // 이메일
        UserRole role,
        UserStatus status,
        Instant issuedAt
) {
    public static LocalLoginResp ofTokensAndUser(
            String accessToken, long accessExpSeconds,
            String refreshToken, long refreshExpSeconds,
            Long userId, String username, UserRole role, UserStatus status,
            Instant issuedAt
    ) {
        return new LocalLoginResp(
                "Bearer",
                accessToken,
                accessExpSeconds,
                refreshToken,
                refreshExpSeconds,
                userId,
                username,
                role,
                status,
                issuedAt
        );
    }
}

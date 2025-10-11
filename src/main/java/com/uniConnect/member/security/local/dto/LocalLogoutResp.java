package com.uniConnect.member.security.local.dto;

import java.time.Instant;

public record LocalLogoutResp(
        String message,
        Instant logoutAt
) {
    public static LocalLogoutResp of(String message) {
        return new LocalLogoutResp(message, Instant.now());
    }
}

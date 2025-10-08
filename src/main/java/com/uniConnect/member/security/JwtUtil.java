package com.uniConnect.member.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.WeakKeyException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

@Component
public class JwtUtil {
    private final SecretKey key;
    private final long expirationMillis;

    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long expirationMillis
    ) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalArgumentException("jwt.secret is empty or missing");
        }
        this.expirationMillis = expirationMillis;

        // Base64 또는 plain 텍스트 모두 허용: 우선 Base64 시도 → 실패 시 plain으로 간주해 Base64 인코딩 후 사용
        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(secret.trim());
        } catch (IllegalArgumentException ex) {
            // Base64가 아니면 plain으로 보고 UTF-8 바이트를 **다시 Base64 인코딩**하여 사용
            keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        }

        // 최소 길이 체크 (HS256 기준 32바이트 이상 권장)
        if (keyBytes.length < 32) {
            throw new WeakKeyException("jwt.secret is too short. Need at least 32 bytes (256 bits) for HS256.");
        }

        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generate(String subject, Map<String, Object> claims) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date(now))
                .expiration(new Date(now + expirationMillis))
                .signWith(key)
                .compact();
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public boolean isValid(String token, String expectedUsername) {
        try {
            Claims c = parseClaims(token);
            return expectedUsername.equals(c.getSubject()) &&
                    c.getExpiration().after(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
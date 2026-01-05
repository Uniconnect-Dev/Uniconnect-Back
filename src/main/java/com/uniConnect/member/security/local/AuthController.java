package com.uniConnect.member.security.local;

import com.uniConnect.member.security.local.dto.*;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import jakarta.servlet.http.Cookie;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthDto.LocalLoginResp> login(@RequestBody AuthDto.LocalLoginReq request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody LocalSignupReq request) {
        authService.signup(request);
        return ResponseEntity.ok("회원가입 성공");
    }

    /**
     * POST /auth/logout
     * JWT 기반 로그아웃
     */
    @PostMapping("/logout")
    public ResponseEntity<AuthDto.LocalLogoutResp> logout(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            HttpServletResponse response,
            @AuthenticationPrincipal CustomUser user
    ) {

        // 1. Authorization 헤더에서 토큰 추출
        String accessToken = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            accessToken = authHeader.substring(7);
        }

        // 2. 현재 인증된 사용자 로그
        if (user != null) {
            log.info("🔴 User {} is logging out", user.getUsername());
        }

        // 3. 서비스 호출 (블랙리스트 등록 등)
        AuthDto.LocalLogoutResp resp = authService.logout(accessToken);

        // 4. 쿠키 삭제 (만약 쿠키로도 토큰을 주고받았다면)
        addExpiredCookie(response, "ACCESS_TOKEN");
        addExpiredCookie(response, "REFRESH_TOKEN");

        log.info("✅ Logout completed");
        return ResponseEntity.ok(resp);
    }

    /**
     * 쿠키 만료 헬퍼 메서드
     */
    private void addExpiredCookie(HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, null);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // HTTPS 환경에서는 true
        response.addCookie(cookie);
    }
}
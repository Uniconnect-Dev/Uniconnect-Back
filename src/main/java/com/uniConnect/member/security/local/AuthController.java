package com.uniConnect.member.security.local;

import com.uniConnect.member.security.local.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
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
@Tag(name="auth API", description= "로그인, 회원가입 관련 API")
public class AuthController {
    private final AuthService authService;

    @Operation(summary = "로그인", description = "JWT 인증을 사용해 계정 로그인합니다.")
    @PostMapping("/login")
    public ResponseEntity<AuthDto.LocalLoginResp> login(@RequestBody AuthDto.LocalLoginReq request) {
        return ResponseEntity.ok(authService.login(request));
    }

    /**
     * POST /auth/logout
     * JWT 기반 로그아웃
     */
    @Operation(summary = "로그아웃", description = "refresh token을 파기하며 로그아웃합니다.")
    @PostMapping("/logout")
    public ResponseEntity<AuthDto.LocalLogoutResp> logout(
            @CookieValue(value="REFRESH_TOKEN", required=false) String refreshToken,
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

    @Operation(summary = "이메일 인증 코드 발송", description = "이메일로 6자리 인증 코드를 발송합니다.")
    @PostMapping("/email/send-code")
    public ResponseEntity<AuthDto.SendVerificationCodeResp> sendVerificationCode(
            @Valid @RequestBody AuthDto.SendVerificationCodeReq request
    ) {
        return ResponseEntity.ok(authService.sendVerificationCode(request));
    }

    @Operation(summary = "이메일 코드 인증", description = "이메일 코드를 입력해 인증합니다.")
    @PostMapping("/email/verify-code")
    public ResponseEntity<AuthDto.VerifyEmailCodeResp> verifyEmailCode(
            @Valid @RequestBody AuthDto.VerifyEmailCodeReq request) {
        return ResponseEntity.ok(authService.verifyEmailCode(request));
    }

    @Operation(summary = "회원가입", description = "필수 사항들을 입력해 회원가입합니다.")
    @PostMapping("/signup")
    public ResponseEntity<AuthDto.SignUpResp> signup(@Valid @RequestBody AuthDto.SignUpReq request) {
        return ResponseEntity.ok(authService.signup(request));
    }
}
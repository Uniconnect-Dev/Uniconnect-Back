package com.uniConnect.member.security.local;

import com.uniConnect.member.entity.LocalCredential;
import com.uniConnect.member.repository.EmailVerificationRepository;
import com.uniConnect.member.repository.LocalCredentialRepository;
import com.uniConnect.member.security.local.dto.*;
import com.uniConnect.member.repository.UserRepository;
import com.uniConnect.member.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository usersRepository;
    private final LocalCredentialRepository localCredentialRepository;
    private final EmailVerificationRepository emailVerificationRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthDto.LocalLoginResp login(AuthDto.LocalLoginReq request) {
        // record 접근자: request.loginId(), request.password()
        LocalCredential cred = localCredentialRepository.findByLoginId(request.getLoginId())
                .orElseThrow(() -> new BadCredentialsException("아이디 또는 비밀번호가 올바르지 않습니다."));

        if (!passwordEncoder.matches(request.getPassword(), cred.getPasswordHash())) {
            throw new BadCredentialsException("아이디 또는 비밀번호가 올바르지 않습니다.");
        }

        User user = cred.getUser();
        Map<String, Object> claims = new HashMap<>();
        // ⭐ 추가
        claims.put("userId", user.getUserId().toString());
        claims.put("username", user.getUsername());
        claims.put("role", user.getRole().name());

        String token = jwtUtil.generate(request.getLoginId(), claims);

        // Build LoginResp (fill known fields; userId can be null if not looked up here)
        return new AuthDto.LocalLoginResp(
                "Bearer",
                token,
                1800L,             // accessTokenExpiresIn (example: 30m)
                null,              // refreshToken (issue via cookie or separate endpoint)
                1209600L,          // refreshTokenExpiresIn (example: 14d)
                user.getUserId(),              // userId (optional lookup if needed)
                request.getLoginId(), // username or login identifier
                user.getRole(),
                user.getStatus(),
                java.time.Instant.now()
        );
    }

    public void signup(AuthDto.SignUpReq request) {
        // record 접근자: request.username(), request.password(), request.name()
        usersRepository.findByUsername(request.getUsername())
            .ifPresent(u -> { throw new IllegalStateException("이미 존재하는 사용자입니다."); });
        if (localCredentialRepository.existsByLoginId(request.loginId())) {
            throw new IllegalStateException("이미 존재하는 로그인 아이디입니다.");
        }

        User user = User.builder()
            .username(request.username())
            .password(null)
            .role(request.userrole())
            .status(request.userStatus())
            .build();
        usersRepository.save(user);
        LocalCredential cred = LocalCredential.builder()
            .user(user)
            .loginId(request.loginId())
            .passwordHash(passwordEncoder.encode(request.getPassword()))
            .build();
        localCredentialRepository.save(cred);

    }

    /**
     * JWT 기반 로그아웃
     * - 실제로는 JWT가 stateless이므로(session 저장x) 서버에서 토큰을 무효화할 수 없음
     * - 클라이언트가 토큰을 삭제하도록 응답만 보냄
     * - (선택) Redis/DB에 블랙리스트 저장 가능
     */
    public AuthDto.LocalLogoutResp logout(String accessToken) {
        log.info("🔴 Logout requested for token: {}...",
                 accessToken != null ? accessToken.substring(0, Math.min(20, accessToken.length())) : "null");
        
        // TODO: (선택) Redis/DB에 토큰 블랙리스트 추가: stateless라 서버에서 무효화x
        // blacklistRepository.save(new TokenBlacklist(accessToken, expiryTime));
        
        return AuthDto.LocalLogoutResp.success("Logged out successfully");
    }
}
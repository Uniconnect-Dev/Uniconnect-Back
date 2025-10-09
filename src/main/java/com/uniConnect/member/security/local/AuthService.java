package com.uniConnect.member.security.local;

import com.uniConnect.member.entity.LocalCredential;
import com.uniConnect.member.enums.UserRole;
import com.uniConnect.member.enums.UserStatus;
import com.uniConnect.member.repository.LocalCredentialRepository;
import com.uniConnect.member.security.local.dto.LocalLoginReq;
import com.uniConnect.member.security.local.dto.LocalLoginResp;
import com.uniConnect.member.security.local.dto.LocalSignupReq;
import com.uniConnect.member.repository.UserRepository;
import com.uniConnect.member.entity.User;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository usersRepository;
    private final LocalCredentialRepository localCredentialRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public LocalLoginResp login(LocalLoginReq request) {
        // record 접근자: request.loginId(), request.password()
        Authentication auth;
        LocalCredential cred = localCredentialRepository.findByLoginId(request.loginId())
                .orElseThrow(() -> new BadCredentialsException("아이디 또는 비밀번호가 올바르지 않습니다."));

        if (!passwordEncoder.matches(request.password(), cred.getPasswordHash())) {
            throw new BadCredentialsException("아이디 또는 비밀번호가 올바르지 않습니다.");
        }
        User user = cred.getUser();
        // 토큰에 원하는 클레임 추가
        Map<String, Object> claims = new HashMap<>();
        String token = jwtUtil.generate(request.loginId(), claims);

        // Build LoginResp (fill known fields; userId can be null if not looked up here)
        return new LocalLoginResp(
                "Bearer",
                token,
                1800L,             // accessTokenExpiresIn (example: 30m)
                null,              // refreshToken (issue via cookie or separate endpoint)
                1209600L,          // refreshTokenExpiresIn (example: 14d)
                user.getUserId(),              // userId (optional lookup if needed)
                request.loginId(), // username or login identifier
                user.getRole(),
                user.getStatus(),
                java.time.Instant.now()
        );
    }

    public void signup(LocalSignupReq request) {
        // record 접근자: request.username(), request.password(), request.name()
        usersRepository.findByUsername(request.username())
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
                .passwordHash(passwordEncoder.encode(request.password()))
                .build();
        localCredentialRepository.save(cred);



    }

    public static void deleteCookie(HttpServletResponse res, String name) {
        Cookie c = new Cookie(name, "");
        c.setPath("/");          // ✅ 생성시와 동일해야 함
        c.setHttpOnly(true);     // 생성시와 동일
        c.setMaxAge(0);          // 삭제
        // c.setSecure(false);   // 로컬 http이면 false, https면 true (생성시와 동일)
        res.addCookie(c);
    }

}
package com.uniConnect.users.security;

import com.uniConnect.repository.UsersRepository;
import com.uniConnect.users.Users;
import com.uniConnect.users.security.dto.*;
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
    private final JwtUtil jwtUtil;
    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;

    public LoginResponse login(LoginRequest request) {
        // record 접근자: request.username(), request.password()
        Authentication auth;
        try {
            auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password())
            );
        } catch (BadCredentialsException e) {
            // 필요시 커스텀 예외로 래핑해도 됨
            throw new BadCredentialsException("아이디 또는 비밀번호가 올바르지 않습니다.");
        }

        // 토큰에 원하는 클레임 추가
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", "ROLE_USER");

        String token = jwtUtil.generate(request.username(), claims);
        return new LoginResponse(token);
    }

    public void signup(SignupRequest request) {
        // record 접근자: request.username(), request.password(), request.name()
        usersRepository.findByUsername(request.username())
                .ifPresent(u -> { throw new IllegalStateException("이미 존재하는 사용자입니다."); });

        Users user = Users.builder()
                .username(request.username())
                .password(passwordEncoder.encode(request.password()))
                .name(request.name())
                .build();

        usersRepository.save(user);
    }
}
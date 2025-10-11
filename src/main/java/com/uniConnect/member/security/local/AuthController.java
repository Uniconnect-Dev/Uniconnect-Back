package com.uniConnect.member.security.local;

import com.uniConnect.member.security.local.dto.*;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LocalLoginResp> login(@RequestBody LocalLoginReq request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody LocalSignupReq request) {
        authService.signup(request);
        return ResponseEntity.ok("회원가입 성공");
    }
}
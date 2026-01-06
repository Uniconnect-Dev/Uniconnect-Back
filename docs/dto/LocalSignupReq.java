package com.uniConnect.member.security.local.dto;

import com.uniConnect.member.enums.UserRole;
import com.uniConnect.member.enums.UserStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record LocalSignupReq(
        @NotBlank @Size(max = 50)
        String username,           // 이메일 -> users.username 에 저장

        @NotBlank @Size(min = 4, max = 50)
        @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "loginId는 영문/숫자/._-만 허용")
        String loginId,            // 화면 아이디 -> local_credentials.login_id

        @NotBlank @Size(min = 8, max = 100)
        String password,            // 평문 입력(서비스에서 BCrypt 해시)

        @NotBlank
        UserRole userrole,

        @NotBlank
        UserStatus userStatus
) {}
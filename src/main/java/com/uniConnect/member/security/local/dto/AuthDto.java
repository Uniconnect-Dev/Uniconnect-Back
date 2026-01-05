package com.uniConnect.member.security.local.dto;

import com.uniConnect.member.enums.UserRole;
import com.uniConnect.member.enums.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public class AuthDto {
    @Data
    @NoArgsConstructor @AllArgsConstructor
    public static class LocalLoginReq {
        @NotBlank @Size(max = 50)
        private String loginId;

        @NotBlank @Size(max = 100)
        private String password;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LocalLoginResp {
        private String tokenType;              // "Bearer"
        private String accessToken;
        private long accessTokenExpiresIn;   // 초 단위
        private String refreshToken;           // (옵션) 쿠키로 줄 거면 null 가능
        private long refreshTokenExpiresIn;  // (옵션) 초 단위

        private Long userId;
        private String username;               // 이메일
        private UserRole role;
        private UserStatus status;
        private Instant issuedAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LocalLogoutResp {
        private boolean success;
        private String message;

        public static LocalLogoutResp success(String message) {
            return LocalLogoutResp.builder()
                    .success(true)
                    .message(message)
                    .build();
        }

        public static LocalLogoutResp fail(String message) {
            return LocalLogoutResp.builder()
                    .success(false)
                    .message(message)
                    .build();
        }
    }

    // ===== 회원가입 =====

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SignUpReq {

        // 회원 정보
        @NotBlank(message = "아이디는 필수입니다")
        @Size(min = 4, max = 20, message = "아이디는 4자 이상 20자 이하여야 합니다")
        @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "아이디는 영문, 숫자, 언더스코어만 사용 가능합니다")
        private String username;

        @NotBlank(message = "비밀번호는 필수입니다")
        @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다")
        @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[!@#$%^&*])[a-zA-Z0-9!@#$%^&*]{8,}$",
                message = "비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다")
        private String password;

        @NotBlank(message = "비밀번호 확인은 필수입니다")
        private String passwordConfirm;

        // 기업 정보
        @NotBlank(message = "브랜드명은 필수입니다")
        private String brandName;

        @NotBlank(message = "담당자 이름은 필수입니다")
        private String managerName;

        @NotBlank(message = "연락처는 필수입니다")
        @Pattern(regexp = "^01[0-9]-?\\d{3,4}-?\\d{4}$", message = "유효한 전화번호 형식이 아닙니다")
        private String phone;

        @NotBlank(message = "이메일은 필수입니다")
        @Email(message = "유효한 이메일 형식이 아닙니다")
        private String email;

        @NotBlank(message = "이메일 인증 코드는 필수입니다")
        private String emailVerificationCode;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SignUpResp {
        private Long userId;
        private String username;
        private String email;
        private String message;
    }

    //verification 관련
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UsernameCheckReq {
        @NotBlank(message = "아이디는 필수입니다")
        private String username;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmailCheckReq {
        @NotBlank(message = "이메일은 필수입니다")
        @Email(message = "유효한 이메일 형식이 아닙니다")
        private String email;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class VerificationResp {
        private boolean available;
        private String message;
        private String status;  // "success", "duplicate", "invalid_format", "error"
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmailVerificationCodeReq {
        @NotBlank(message = "이메일은 필수입니다")
        @Email(message = "유효한 이메일 형식이 아닙니다")
        private String email;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmailVerificationCodeCheckReq {
        @NotBlank(message = "이메일은 필수입니다")
        private String email;

        @NotBlank(message = "인증 코드는 필수입니다")
        @Size(min = 6, max = 6, message = "인증 코드는 6자리입니다")
        private String code;
    }
}

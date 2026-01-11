package com.uniConnect.member.security.local;

import com.uniConnect.common.service.EmailService;
import com.uniConnect.common.service.impl.EmailServiceImpl;
import com.uniConnect.company.entity.Company;
import com.uniConnect.company.repository.CompanyRepository;
import com.uniConnect.member.entity.BusinessRegistration;
import com.uniConnect.member.entity.EmailVerification;
import com.uniConnect.member.entity.LocalCredential;
import com.uniConnect.member.enums.UserStatus;
import com.uniConnect.member.enums.VerifiedStatus;
import com.uniConnect.member.repository.BusinessRegistrationRepository;
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

import java.time.LocalDateTime;
import java.util.*;

import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository usersRepository;
    private final CompanyRepository companyRepository;
    private final LocalCredentialRepository localCredentialRepository;
    private final EmailVerificationRepository emailVerificationRepository;
    private final BusinessRegistrationRepository businessRegistrationRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailService emailService; //구현체가 아닌 추상(DIP)에 의존

    private static final int CODE_EXPIRY_MINUTES = 5;
    private static final int RESEND_COOLDOWN_SECONDS = 30;
    private static final int MAX_ATTEMPTS = 5;


    public AuthDto.LocalLoginResp login(AuthDto.LocalLoginReq request) {
        // record 접근자: request.loginId(), request.password()
        LocalCredential cred = localCredentialRepository.findByLoginId(request.getLoginId())
                .orElseThrow(() -> new BadCredentialsException("아이디가 올바르지 않습니다."));

        if (!passwordEncoder.matches(request.getPassword(), cred.getPasswordHash())) {
            throw new BadCredentialsException("비밀번호가 올바르지 않습니다.");
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

    //이메일 인증: 인증코드 이메일로 발송(코드 생성)-> 입력 검증해 인증
    public AuthDto.SendVerificationCodeResp sendVerificationCode(AuthDto.SendVerificationCodeReq request) {
        String email = request.getEmail();

        // 1) 기존 레코드 조회
        EmailVerification existing = emailVerificationRepository.findByEmail(email).orElse(null);

        // 2) 재전송 쿨타임 확인 (30초 내 재전송 금지)
        if (existing != null && existing.getLastSentAt() != null) {
            long secondsSinceLastSent = java.time.Duration
                    .between(existing.getLastSentAt(), LocalDateTime.now())
                    .getSeconds();
            if (secondsSinceLastSent < RESEND_COOLDOWN_SECONDS) {
                throw new IllegalStateException(
                        String.format("%d초 후에 다시 시도해주세요", RESEND_COOLDOWN_SECONDS - secondsSinceLastSent)
                );
            }
        }

        // 3) 6자리 코드 생성
        String code = String.format("%06d", new java.util.Random().nextInt(1000000));
        String codeHash = passwordEncoder.encode(code);  // 해시 저장

        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(CODE_EXPIRY_MINUTES);

        // 4) 업서트 (upd 또는 insert)
        if (existing != null) {
            existing.setCodeHash(codeHash);
            existing.setExpiresAt(expiresAt);
            existing.setVerified(false);
            existing.setVerifiedAt(null);
            existing.setAttemptCount(0);
            existing.setLastSentAt(LocalDateTime.now());
            emailVerificationRepository.save(existing); //해당 코드에 맞는 emailVeri 생성
        } else {
            EmailVerification newRecord = EmailVerification.builder()
                    .email(email)
                    .codeHash(codeHash)
                    .expiresAt(expiresAt)
                    .verified(false)
                    .attemptCount(0)
                    .lastSentAt(LocalDateTime.now())
                    .build();
            emailVerificationRepository.save(newRecord);
        }

        // 5) 실제 이메일 전송 (구현 필요)
        emailService.sendVerificationCode(email, code);
        log.info("✉️ 인증 코드 발송: {} (테스트용 코드: {})", email, code);

        return AuthDto.SendVerificationCodeResp.builder()
                .success(true)
                .message("인증 코드가 이메일로 전송되었습니다")
                .build();
    }

    /**
     * 이메일 인증 코드 검증
     */
    public AuthDto.VerifyEmailCodeResp verifyEmailCode(AuthDto.VerifyEmailCodeReq request) {
        String email = request.getEmail();
        String code = request.getCode();

        // 1) 이메일로 레코드 조회
        EmailVerification record = emailVerificationRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("인증 코드가 발송되지 않았습니다"));

        // 2) 만료 확인
        if (LocalDateTime.now().isAfter(record.getExpiresAt())) {
            throw new IllegalStateException("인증 코드가 만료되었습니다");
        }

        // 3) 시도 횟수 확인
        if (record.getAttemptCount() >= MAX_ATTEMPTS) {
            throw new IllegalStateException("인증 시도 횟수를 초과했습니다. 새 코드를 요청해주세요");
        }

        // 4) 코드 검증 (해시 비교)
        if (!passwordEncoder.matches(code, record.getCodeHash())) {
            record.setAttemptCount(record.getAttemptCount() + 1);
            emailVerificationRepository.save(record);
            throw new BadCredentialsException("인증 코드가 일치하지 않습니다");
        }

        // 5) 인증 완료 처리
        record.setVerified(true);
        record.setVerifiedAt(LocalDateTime.now());
        record.setAttemptCount(0);
        emailVerificationRepository.save(record);

        log.info("✅ 이메일 인증 완료: {}", email);

        return AuthDto.VerifyEmailCodeResp.builder()
                .success(true)
                .message("이메일 인증이 완료되었습니다")
                .build();
    }

    /**
     * 회원가입 (id, pw, 이메일 검증 필수)
     */
    public AuthDto.SignUpResp signup(AuthDto.SignUpReq request) {
        // 1) 비밀번호 확인 검증
        if (!request.getPassword().equals(request.getPasswordConfirm())) {
            throw new IllegalStateException("비밀번호가 일치하지 않습니다");
        }

        // 2) 이메일 인증 검증 (verified=true인지 확인)
        EmailVerification verification = emailVerificationRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalStateException("이메일 인증이 필요합니다"));

        if (!Boolean.TRUE.equals(verification.getVerified())) {
            throw new IllegalStateException("이메일이 인증되지 않았습니다");
        }

        // 3) 인증 시간 유효성 확인 (선택: 인증 후 1시간 이내만 가입 허용)
        if (LocalDateTime.now().isAfter(verification.getVerifiedAt().plusHours(1))) {
            throw new IllegalStateException("인증이 만료되었습니다. 다시 인증해주세요");
        }

        // 4) 기존 사용자 확인 (기존 코드)
        if (usersRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalStateException("이미 존재하는 아이디입니다");
        }
        if (localCredentialRepository.existsByLoginId(request.getUsername())) {
            throw new IllegalStateException("이미 존재하는 아이디입니다");
        }

        // 5) User 생성
        User user = User.builder()
                .username(request.getUsername())
                .password(null)  // 보안: plaintext 저장 ❌
                .role(request.getUserrole())
                .status(request.getUserStatus() != null ? request.getUserStatus() : UserStatus.Active)
                .build();
        user = usersRepository.save(user);

        // 6) LocalCredential 생성
        LocalCredential cred = LocalCredential.builder()
                .user(user)
                .loginId(request.getUsername())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .build();
        localCredentialRepository.save(cred);

        // 7) 인증 레코드 삭제 (또는 used 처리)
        emailVerificationRepository.deleteByEmail(request.getEmail());

        log.info("✅ 회원가입 완료: {}", request.getUsername());

        return AuthDto.SignUpResp.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(request.getEmail())
                .message("회원가입이 완료되었습니다")
                .build();
    }

    public AuthDto.SignUpResp signupWithoutEmail(AuthDto.SignUpReqWithoutEmail request) {
        // 1) 비밀번호 확인 검증
        if (!request.getPassword().equals(request.getPasswordConfirm())) {
            throw new IllegalStateException("비밀번호가 일치하지 않습니다");
        }

//        // 2) 이메일 인증 검증 (verified=true인지 확인)
//        EmailVerification verification = emailVerificationRepository.findByEmail(request.getEmail())
//                .orElseThrow(() -> new IllegalStateException("이메일 인증이 필요합니다"));
//
//        if (!Boolean.TRUE.equals(verification.getVerified())) {
//            throw new IllegalStateException("이메일이 인증되지 않았습니다");
//        }
//
//        // 3) 인증 시간 유효성 확인 (선택: 인증 후 1시간 이내만 가입 허용)
//        if (LocalDateTime.now().isAfter(verification.getVerifiedAt().plusHours(1))) {
//            throw new IllegalStateException("인증이 만료되었습니다. 다시 인증해주세요");
//        }

        // 4) 기존 사용자 확인 (기존 코드)
        if (usersRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalStateException("이미 존재하는 아이디입니다");
        }
        if (localCredentialRepository.existsByLoginId(request.getUsername())) {
            throw new IllegalStateException("이미 존재하는 아이디입니다");
        }

        // 5) User 생성
        User user = User.builder()
                .username(request.getUsername())
                .password(null)  // 보안: plaintext 저장 ❌
                .role(request.getUserrole())
                .status(request.getUserStatus() != null ? request.getUserStatus() : UserStatus.Active)
                .build();
        user = usersRepository.save(user);

        // 6) LocalCredential 생성
        LocalCredential cred = LocalCredential.builder()
                .user(user)
                .loginId(request.getUsername())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .build();
        localCredentialRepository.save(cred);

//        // 7) 인증 레코드 삭제 (또는 used 처리)
//        emailVerificationRepository.deleteByEmail(request.getEmail());

        log.info("✅ 회원가입 완료: {}", request.getUsername());

        return AuthDto.SignUpResp.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(request.getEmail())
                .message("회원가입이 완료되었습니다")
                .build();
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

    /**
     * 사업자 등록 정보 생성
     */
    public AuthDto.BusinessRegistrationResponse createBusinessRegistration(AuthDto.BusinessRegistrationReq dto) {
//        User user = usersRepository.findById(dto.getUsersId())
//                .orElseThrow(() -> new RuntimeException("User not found"));

        Company company = companyRepository.findById(dto.getCompanyId())
                .orElseThrow(() -> new RuntimeException("Company not found"));

        BusinessRegistration businessRegistration = BusinessRegistration.builder()
                .registrationNo(dto.getRegistrationNo())
                .companyName(dto.getCompanyName())
                .representativeName(dto.getRepresentativeName())
                .openDate(dto.getOpenDate())
                .bizType(dto.getBizType())
                .bizItem(dto.getBizItem())
                .certificateUrl(dto.getCertificateUrl())
                .verifiedStatus(VerifiedStatus.Pending)
//                .user(user)
                .company(company)
                .build();

        BusinessRegistration saved = businessRegistrationRepository.save(businessRegistration);
        return convertToResponseDTO(saved);
    }

    /**
     * 사업자 등록 정보 조회 (ID로)
     */
    @Transactional(readOnly = true)
    public AuthDto.BusinessRegistrationResponse getBusinessRegistration(Long registrationId) {
        BusinessRegistration businessRegistration = businessRegistrationRepository.findById(registrationId)
                .orElseThrow(() -> new RuntimeException("Business Registration not found"));
        return convertToResponseDTO(businessRegistration);
    }

    /**
     * 회사별 사업자 등록 정보 조회
     */
    @Transactional(readOnly = true)
    public AuthDto.BusinessRegistrationResponse getBusinessRegistrationByCompany(Long companyId) {
        BusinessRegistration businessRegistration = businessRegistrationRepository.findByCompanyCompanyId(companyId)
                .orElseThrow(() -> new RuntimeException("Business Registration not found for company"));
        return convertToResponseDTO(businessRegistration);
    }

//    /**
//     * 사용자별 사업자 등록 정보 조회
//     */
//    @Transactional(readOnly = true)
//    public AuthDto.BusinessRegistrationResponse getBusinessRegistrationByUser(Long userId) {
//        BusinessRegistration businessRegistration = businessRegistrationRepository.findByUserUserId(userId)
//                .orElseThrow(() -> new RuntimeException("Business Registration not found for user"));
//        return convertToResponseDTO(businessRegistration);
//    }

    /**
     * 사업자등록번호로 조회
     */
    @Transactional(readOnly = true)
    public AuthDto.BusinessRegistrationResponse getBusinessRegistrationByRegistrationNo(String registrationNo) {
        BusinessRegistration businessRegistration = businessRegistrationRepository.findByRegistrationNo(registrationNo)
                .orElseThrow(() -> new RuntimeException("Business Registration not found"));
        return convertToResponseDTO(businessRegistration);
    }

    /**
     * 사업자 등록 정보 업데이트
     */
    public AuthDto.BusinessRegistrationResponse updateBusinessRegistration(Long registrationId, AuthDto.BusinessRegistrationReq dto) {
        BusinessRegistration businessRegistration = businessRegistrationRepository.findById(registrationId)
                .orElseThrow(() -> new RuntimeException("Business Registration not found"));

        businessRegistration.setRegistrationNo(dto.getRegistrationNo());
        businessRegistration.setCompanyName(dto.getCompanyName());
        businessRegistration.setRepresentativeName(dto.getRepresentativeName());
        businessRegistration.setOpenDate(dto.getOpenDate());
        businessRegistration.setBizType(dto.getBizType());
        businessRegistration.setBizItem(dto.getBizItem());
        businessRegistration.setCertificateUrl(dto.getCertificateUrl());

        BusinessRegistration updated = businessRegistrationRepository.save(businessRegistration);
        return convertToResponseDTO(updated);
    }

    /**
     * 사업자 등록 정보 검증 승인
     */
    public AuthDto.BusinessRegistrationResponse verifyBusinessRegistration(Long registrationId) {
        BusinessRegistration businessRegistration = businessRegistrationRepository.findById(registrationId)
                .orElseThrow(() -> new RuntimeException("Business Registration not found"));

        businessRegistration.setVerifiedStatus(VerifiedStatus.Verified);
        businessRegistration.setVerifiedAt(LocalDateTime.now());

        BusinessRegistration updated = businessRegistrationRepository.save(businessRegistration);
        return convertToResponseDTO(updated);
    }

    /**
     * 사업자 등록 정보 검증 거절
     */
    public AuthDto.BusinessRegistrationResponse rejectBusinessRegistration(Long registrationId) {
        BusinessRegistration businessRegistration = businessRegistrationRepository.findById(registrationId)
                .orElseThrow(() -> new RuntimeException("Business Registration not found"));

        businessRegistration.setVerifiedStatus(VerifiedStatus.Rejected);
        businessRegistration.setVerifiedAt(LocalDateTime.now());

        BusinessRegistration updated = businessRegistrationRepository.save(businessRegistration);
        return convertToResponseDTO(updated);
    }

    /**
     * 사업자 등록 정보 삭제
     */
    public void deleteBusinessRegistration(Long registrationId) {
        businessRegistrationRepository.deleteById(registrationId);
    }

    /**
     * DTO 변환 메서드
     */
    private AuthDto.BusinessRegistrationResponse convertToResponseDTO(BusinessRegistration businessRegistration) {
        return AuthDto.BusinessRegistrationResponse.builder()
                .registrationId(businessRegistration.getRegistrationId())
                .registrationNo(businessRegistration.getRegistrationNo())
                .companyName(businessRegistration.getCompanyName())
                .representativeName(businessRegistration.getRepresentativeName())
                .openDate(businessRegistration.getOpenDate())
                .bizType(businessRegistration.getBizType())
                .bizItem(businessRegistration.getBizItem())
                .certificateUrl(businessRegistration.getCertificateUrl())
                .verifiedStatus(businessRegistration.getVerifiedStatus())
                .verifiedAt(businessRegistration.getVerifiedAt())
                .userId(businessRegistration.getUser().getUserId())
                .companyId(businessRegistration.getCompany().getCompanyId())
                .build();
    }
}
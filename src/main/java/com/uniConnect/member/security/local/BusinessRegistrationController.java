package com.uniConnect.member.security.local;

import com.uniConnect.member.security.local.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/business-registrations")
@RequiredArgsConstructor
public class BusinessRegistrationController {

    private final AuthService authService;

    /**
     * 사업자 등록 정보 생성
     */
    @PostMapping
    public ResponseEntity<AuthDto.BusinessRegistrationResponse> createBusinessRegistration(
            @RequestBody AuthDto.BusinessRegistrationReq dto) {
        AuthDto.BusinessRegistrationResponse response = authService.createBusinessRegistration(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 사업자 등록 정보 조회 (ID로)
     */
    @GetMapping("/{registrationId}")
    public ResponseEntity<AuthDto.BusinessRegistrationResponse> getBusinessRegistration(
            @PathVariable Long registrationId) {
        AuthDto.BusinessRegistrationResponse response = authService.getBusinessRegistration(registrationId);
        return ResponseEntity.ok(response);
    }

    /**
     * 회사별 사업자 등록 정보 조회
     */
    @GetMapping("/company/{companyId}")
    public ResponseEntity<AuthDto.BusinessRegistrationResponse> getBusinessRegistrationByCompany(
            @PathVariable Long companyId) {
        AuthDto.BusinessRegistrationResponse response = authService.getBusinessRegistrationByCompany(companyId);
        return ResponseEntity.ok(response);
    }

//    /**
//     * 사용자별 사업자 등록 정보 조회
//     */
//    @GetMapping("/user/{userId}")
//    public ResponseEntity<AuthDto.BusinessRegistrationResponse> getBusinessRegistrationByUser(
//            @PathVariable Long userId) {
//        AuthDto.BusinessRegistrationResponse response = authService.getBusinessRegistrationByUser(userId);
//        return ResponseEntity.ok(response);
//    }

    /**
     * 사업자등록번호로 조회
     */
    @GetMapping("/number/{registrationNo}")
    public ResponseEntity<AuthDto.BusinessRegistrationResponse> getBusinessRegistrationByNumber(
            @PathVariable String registrationNo) {
        AuthDto.BusinessRegistrationResponse response = authService.getBusinessRegistrationByRegistrationNo(registrationNo);
        return ResponseEntity.ok(response);
    }

    /**
     * 사업자 등록 정보 업데이트
     */
    @PutMapping("/{registrationId}")
    public ResponseEntity<AuthDto.BusinessRegistrationResponse> updateBusinessRegistration(
            @PathVariable Long registrationId,
            @RequestBody AuthDto.BusinessRegistrationReq dto) {
        AuthDto.BusinessRegistrationResponse response = authService.updateBusinessRegistration(registrationId, dto);
        return ResponseEntity.ok(response);
    }

    /**
     * 사업자 등록 정보 검증 승인
     */
    @PatchMapping("/{registrationId}/verify")
    public ResponseEntity<AuthDto.BusinessRegistrationResponse> verifyBusinessRegistration(
            @PathVariable Long registrationId) {
        AuthDto.BusinessRegistrationResponse response = authService.verifyBusinessRegistration(registrationId);
        return ResponseEntity.ok(response);
    }

    /**
     * 사업자 등록 정보 검증 거절
     */
    @PatchMapping("/{registrationId}/reject")
    public ResponseEntity<AuthDto.BusinessRegistrationResponse> rejectBusinessRegistration(
            @PathVariable Long registrationId) {
        AuthDto.BusinessRegistrationResponse response = authService.rejectBusinessRegistration(registrationId);
        return ResponseEntity.ok(response);
    }

    /**
     * 사업자 등록 정보 삭제
     */
    @DeleteMapping("/{registrationId}")
    public ResponseEntity<Void> deleteBusinessRegistration(
            @PathVariable Long registrationId) {
        authService.deleteBusinessRegistration(registrationId);
        return ResponseEntity.noContent().build();
    }
}

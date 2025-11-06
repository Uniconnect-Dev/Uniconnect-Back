package com.uniConnect.profile.controller;

import com.uniConnect.global.response.ApiResponse;
import com.uniConnect.member.security.local.CustomUser;
import com.uniConnect.profile.dto.CompanyInitRequest;
import com.uniConnect.profile.dto.CompanyGetResponse;
import com.uniConnect.profile.dto.CompanyUpdateRequest;
import com.uniConnect.profile.service.CompanyProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile/company")
@RequiredArgsConstructor
@Tag(name = "Company Profile", description = "기업 회원 프로필 관리 API")
public class CompanyProfileController {

    private final CompanyProfileService companyProfileService;

    @PostMapping("/init")
    @Operation(summary = "기업 프로필 초기 생성", description = "필수 정보: 브랜드명, 산업 분야")
    public ResponseEntity<ApiResponse<CompanyGetResponse>> initProfile(
            @AuthenticationPrincipal CustomUser customUser,
            @RequestBody CompanyInitRequest request
    ) {
        CompanyGetResponse response = companyProfileService.initProfile(customUser.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<CompanyGetResponse>> getProfile(
            @AuthenticationPrincipal CustomUser customUser
    ) {
        CompanyGetResponse response = companyProfileService.getProfile(customUser.getUserId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<CompanyGetResponse>> updateProfile(
            @AuthenticationPrincipal CustomUser customUser,
            @RequestBody CompanyUpdateRequest request
    ) {
        CompanyGetResponse response = companyProfileService.updateProfile(customUser.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
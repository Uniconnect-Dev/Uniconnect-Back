package com.uniConnect.profile.controller;

import com.uniConnect.global.response.ApiResponse;
import com.uniConnect.member.security.local.CustomUser;
import com.uniConnect.profile.dto.CompanyInitRequest;
import com.uniConnect.profile.dto.CompanyGetResponse;
import com.uniConnect.profile.dto.CompanyUpdateRequest;
import com.uniConnect.profile.service.CompanyProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile/company")
@RequiredArgsConstructor
public class CompanyProfileController {

    private final CompanyProfileService companyProfileService;

    @PostMapping("/init")
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
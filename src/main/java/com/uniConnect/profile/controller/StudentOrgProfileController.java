package com.uniConnect.profile.controller;

import com.uniConnect.global.response.ApiResponse;
import com.uniConnect.member.security.local.CustomUser;
import com.uniConnect.profile.dto.StudentOrgInitRequest;
import com.uniConnect.profile.dto.StudentOrgGetResponse;
import com.uniConnect.profile.dto.StudentOrgUpdateRequest;
import com.uniConnect.profile.service.StudentOrgProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile/student-org")
@RequiredArgsConstructor
public class StudentOrgProfileController {

    private final StudentOrgProfileService studentOrgProfileService;

    @PostMapping("/init")
    public ResponseEntity<ApiResponse<StudentOrgGetResponse>> initProfile(
            @AuthenticationPrincipal CustomUser customUser,
            @RequestBody StudentOrgInitRequest request
    ) {
        StudentOrgGetResponse response = studentOrgProfileService.initProfile(customUser.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<StudentOrgGetResponse>> getProfile(
            @AuthenticationPrincipal CustomUser customUser
    ) {
        StudentOrgGetResponse response = studentOrgProfileService.getProfile(customUser.getUserId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<StudentOrgGetResponse>> updateProfile(
            @AuthenticationPrincipal CustomUser customUser,
            @RequestBody StudentOrgUpdateRequest request
    ) {
        StudentOrgGetResponse response = studentOrgProfileService.updateProfile(customUser.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
package com.uniConnect.profile.controller;

import com.uniConnect.global.response.ApiResponse;
import com.uniConnect.member.security.local.CustomUser;
import com.uniConnect.profile.dto.StudentOrgInitRequest;
import com.uniConnect.profile.dto.StudentOrgGetResponse;
import com.uniConnect.profile.dto.StudentOrgUpdateRequest;
import com.uniConnect.profile.service.StudentOrgProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile/student-org")
@RequiredArgsConstructor
@Tag(name = "Student Organization Profile", description = "학생 단체 프로필 관리 API")
public class StudentOrgProfileController {

    private final StudentOrgProfileService studentOrgProfileService;

    @PostMapping("/init")
    @Operation(summary = "학생 단체 프로필 초기 생성")
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
package com.uniConnect.matching.controller;

import com.uniConnect.global.response.ApiResponse;
import com.uniConnect.matching.dto.MatchReceivedItemResponse;
import com.uniConnect.matching.dto.MatchSentItemResponse;
import com.uniConnect.matching.dto.MatchStatusSummaryResponse;
import com.uniConnect.matching.service.CollaborationMatchQueryService;
import com.uniConnect.member.security.local.CustomUser;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/matching/status")
@Tag(name = "Matching Status", description = "매칭 상태 조회 API")
public class CollaborationMatchQueryController {

    private final CollaborationMatchQueryService service;

    // ===============================
    // 학생단체용
    // ===============================

    @GetMapping("/student/summary")
    @Operation(summary = "학생단체 매칭 요약 조회")
    public ApiResponse<MatchStatusSummaryResponse> studentSummary(
            @AuthenticationPrincipal CustomUser user
    ) {
        Long studentOrgId = getStudentOrgId(user);
        return ApiResponse.success(service.getStudentOrgSummary(studentOrgId));
    }

    @GetMapping("/student/sent")
    @Operation(summary = "학생단체 - 내가 요청한 매칭 목록")
    public ApiResponse<List<MatchSentItemResponse>> studentSent(
            @AuthenticationPrincipal CustomUser user
    ) {
        Long studentOrgId = getStudentOrgId(user);
        return ApiResponse.success(service.getStudentOrgSentList(studentOrgId));
    }

    @GetMapping("/student/received")
    @Operation(summary = "학생단체 - 받은 매칭 목록")
    public ApiResponse<List<MatchReceivedItemResponse>> studentReceived(
            @AuthenticationPrincipal CustomUser user
    ) {
        Long studentOrgId = getStudentOrgId(user);
        return ApiResponse.success(service.getStudentOrgReceivedList(studentOrgId));
    }

    // ===============================
    // 기업용
    // ===============================

    @GetMapping("/company/summary")
    @Operation(summary = "기업 매칭 요약 조회")
    public ApiResponse<MatchStatusSummaryResponse> companySummary(
            @AuthenticationPrincipal CustomUser user
    ) {
        Long companyId = getCompanyId(user);
        return ApiResponse.success(service.getCompanySummary(companyId));
    }

    @GetMapping("/company/sent")
    @Operation(summary = "기업 - 내가 요청한 매칭 목록")
    public ApiResponse<List<MatchSentItemResponse>> companySent(
            @AuthenticationPrincipal CustomUser user
    ) {
        Long companyId = getCompanyId(user);
        return ApiResponse.success(service.getCompanySentList(companyId));
    }

    @GetMapping("/company/received")
    @Operation(summary = "기업 - 받은 매칭 목록")
    public ApiResponse<List<MatchReceivedItemResponse>> companyReceived(
            @AuthenticationPrincipal CustomUser user
    ) {
        Long companyId = getCompanyId(user);
        return ApiResponse.success(service.getCompanyReceivedList(companyId));
    }

    // ===============================
    // 내부 공통 메서드
    // ===============================

    private Long getStudentOrgId(CustomUser user) {
        validateUser(user);
        return service.findStudentOrgIdByUserId(user.getUserId());
    }

    private Long getCompanyId(CustomUser user) {
        validateUser(user);
        return service.findCompanyIdByUserId(user.getUserId());
    }

    private void validateUser(CustomUser user) {
        if (user == null || user.getUserId() == null) {
            throw new IllegalStateException("인증 정보가 없습니다. JWT 토큰을 확인하세요.");
        }
    }
}

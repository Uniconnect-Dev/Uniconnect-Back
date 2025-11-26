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
            @AuthenticationPrincipal CustomUser user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Long studentOrgId = getStudentOrgId(user);
        return ApiResponse.success(service.getStudentOrgSentList(studentOrgId, page, size));
    }

    @GetMapping("/student/received")
    @Operation(summary = "학생단체 - 받은 매칭 목록")
    public ApiResponse<List<MatchReceivedItemResponse>> studentReceived(
            @AuthenticationPrincipal CustomUser user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Long studentOrgId = getStudentOrgId(user);
        return ApiResponse.success(service.getStudentOrgReceivedList(studentOrgId, page, size));
    }

    @PostMapping("/student/{matchId}/approve")
    @Operation(summary = "학생단체 - 매칭 요청 승인")
    public ApiResponse<String> approveMatchByStudent(
            @AuthenticationPrincipal CustomUser user,
            @PathVariable Long matchId
    ) {
        Long studentOrgId = getStudentOrgId(user);
        service.approveMatchByStudent(studentOrgId, matchId);
        return ApiResponse.success("학생단체가 매칭을 승인했습니다.");
    }

    @PostMapping("/student/{matchId}/reject")
    @Operation(summary = "학생단체 - 매칭 요청 거절")
    public ApiResponse<String> rejectMatchByStudent(
            @AuthenticationPrincipal CustomUser user,
            @PathVariable Long matchId
    ) {
        Long studentOrgId = getStudentOrgId(user);
        service.rejectMatchByStudent(studentOrgId, matchId);
        return ApiResponse.success("학생단체가 매칭을 거절했습니다.");
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
            @AuthenticationPrincipal CustomUser user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Long companyId = getCompanyId(user);
        return ApiResponse.success(service.getCompanySentList(companyId, page, size));
    }

    @GetMapping("/company/received")
    @Operation(summary = "기업 - 받은 매칭 목록")
    public ApiResponse<List<MatchReceivedItemResponse>> companyReceived(
            @AuthenticationPrincipal CustomUser user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Long companyId = getCompanyId(user);
        return ApiResponse.success(service.getCompanyReceivedList(companyId, page, size));
    }

    @PostMapping("/company/{matchId}/approve")
    @Operation(summary = "기업 - 매칭 요청 승인")
    public ApiResponse<String> approveMatchByCompany(
            @AuthenticationPrincipal CustomUser user,
            @PathVariable Long matchId
    ) {
        Long companyId = getCompanyId(user);
        service.approveMatchByCompany(companyId, matchId);
        return ApiResponse.success("기업이 매칭을 승인했습니다.");
    }

    @PostMapping("/company/{matchId}/reject")
    @Operation(summary = "기업 - 매칭 요청 거절")
    public ApiResponse<String> rejectMatchByCompany(
            @AuthenticationPrincipal CustomUser user,
            @PathVariable Long matchId
    ) {
        Long companyId = getCompanyId(user);
        service.rejectMatchByCompany(companyId, matchId);
        return ApiResponse.success("기업이 매칭을 거절했습니다.");
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

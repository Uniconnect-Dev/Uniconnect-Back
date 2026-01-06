package com.uniConnect.campaign.controller;

import com.uniConnect.global.response.ApiResponse;
import com.uniConnect.matching.dto.CompanySelectRequest;
import com.uniConnect.matching.service.CollaborationMatchRequestService;
import com.uniConnect.member.security.local.CustomUser;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/campaigns")
@Tag(name = "Campaign Matching", description = "캠페인 기준 매칭 요청 생성 API")
public class CampaignMatchRequestController {

    private final CollaborationMatchRequestService matchRequestService;

    @PostMapping("/{campaignId}/match-requests")
    @Operation(summary = "학생단체 → 기업 매칭 요청 생성")
    public ApiResponse<Void> createMatchRequests(
            @PathVariable Long campaignId,
            @AuthenticationPrincipal CustomUser user,
            @RequestBody CompanySelectRequest request
    ) {
        if (user == null || user.getUserId() == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }

        matchRequestService.createMatchRequests(
                campaignId,
                user.getUserId(),
                request.companyIds()
        );

        return ApiResponse.success("매칭 요청 생성 완료", null);
    }
}

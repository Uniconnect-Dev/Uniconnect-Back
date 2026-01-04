package com.uniConnect.campaign.controller;

import com.uniConnect.campaign.dto.CampaignCreateRequest;
import com.uniConnect.campaign.dto.CampaignFirstPageResponse;
import com.uniConnect.campaign.dto.CampaignFirstPageSaveRequest;
import com.uniConnect.campaign.service.CampaignService;
import com.uniConnect.global.response.ApiResponse;
import com.uniConnect.member.security.local.CustomUser;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/campaigns")
@Tag(name = "Campaign", description = "학생단체 → 기업 협업 요청 생성 및 관리 API")
public class CampaignController {

    private final CampaignService campaignService;

    /**
     * 캠페인 생성 첫 페이지 조회
     */
    @GetMapping("/first-page")
    @Operation(summary = "첫 페이지 기본 정보 조회")
    public ResponseEntity<CampaignFirstPageResponse> getFirstPage(
            @AuthenticationPrincipal CustomUser user
    ) {
        if (user == null || user.getUserId() == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }

        return ResponseEntity.ok(
                campaignService.getFirstPage(user.getUserId())
        );
    }

    /**
     * 캠페인 생성 첫 페이지 저장
     */
    @PostMapping("/first-page")
    @Operation(summary = "첫 페이지 기본 정보 저장")
    public ApiResponse<Long> saveFirstPage(
            @AuthenticationPrincipal CustomUser user,
            @RequestBody CampaignFirstPageSaveRequest request
    ) {
        if (user == null || user.getUserId() == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }

        Long campaignId = campaignService.saveFirstPage(
                user.getUserId(),
                request
        );

        return ApiResponse.success("캠페인 첫 페이지 저장 완료", campaignId);
    }

    /**
     * 캠페인 생성 (한 페이지 입력)
     */
    @PostMapping
    @Operation(summary = "협업 요청 정보 입력")
    public ApiResponse<Long> createCampaign(
            @AuthenticationPrincipal CustomUser user,
            @RequestBody CampaignCreateRequest request
    ) {
        if (user == null || user.getUserId() == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }

        Long campaignId = campaignService.createCampaign(
                user.getUserId(),
                request
        );

        return ApiResponse.success("협업 생성 완료", campaignId);
    }

    /**
     * 협업 제안서 업로드
     */
    @PostMapping(
            value = "/{campaignId}/proposal",
            consumes = "multipart/form-data"
    )
    @Operation(summary = "협업 제안서 업로드")
    public ApiResponse<String> uploadCampaignProposal(
            @PathVariable Long campaignId,
            @AuthenticationPrincipal CustomUser user,
            @RequestPart("file") MultipartFile file
    ) {
        if (user == null || user.getUserId() == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }

        String url = campaignService.uploadProposalFile(
                campaignId,
                user.getUserId(),
                file
        );

        return ApiResponse.success("제안서 업로드 완료", url);
    }

    /**
     * 협업 최종 제출
     */
    @PostMapping("/{campaignId}/submit")
    @Operation(summary = "협업 최종 제출")
    public ApiResponse<Void> submitCampaign(
            @PathVariable Long campaignId,
            @AuthenticationPrincipal CustomUser user
    ) {
        if (user == null || user.getUserId() == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }

        campaignService.submit(
                campaignId,
                user.getUserId()
        );

        return ApiResponse.success("협업 제출 완료", null);
    }
}

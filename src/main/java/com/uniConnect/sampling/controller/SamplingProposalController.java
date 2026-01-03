package com.uniConnect.sampling.controller;

import com.uniConnect.global.response.ApiResponse;
import com.uniConnect.member.security.local.CustomUser;
import com.uniConnect.sampling.dto.*;
import com.uniConnect.sampling.service.SamplingProposalService;
import com.uniConnect.sampling.service.SamplingProposalTargetService;
import com.uniConnect.sampling.service.SamplingMatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


import java.util.List;
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sampling/proposals")
@Tag(name = "Sampling Proposal", description = "[기업->학생단체] 샘플링 매칭 기능 API")
public class SamplingProposalController {

    private final SamplingProposalService samplingProposalService;
    private final SamplingProposalTargetService targetService;
    private final SamplingMatchService samplingMatchService;

    @PostMapping
    @Operation(summary = "1페이지: 샘플링 요청 생성")
    public ApiResponse<Long> createProposal(
            @AuthenticationPrincipal CustomUser user,
            @RequestBody SamplingProposalCreateRequest request
    ) {
        if (user == null || user.getUserId() == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }

        Long proposalId =
                samplingProposalService.createProposal(user.getUserId(), request);

        return ApiResponse.success("샘플링 요청 생성 완료", proposalId);
    }

    @PostMapping("/targets")
    @Operation(summary = "2페이지: 기업 샘플링 요청 타깃 저장")
    public ApiResponse<Void> saveTargets(
            @AuthenticationPrincipal CustomUser user,
            @RequestBody SamplingProposalTargetRequestDto dto
    ) {
        if (user == null || user.getUserId() == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }

        targetService.saveTargets(user.getUserId(), dto);
        return ApiResponse.success("타깃 저장 완료", null);
    }

    /* =========================
       3페이지: 매칭 학생단체 조회
    ========================= */

    @GetMapping("/{samplingProposalId}/student-orgs")
    @Operation(summary = "3페이지: 매칭 학생단체 조회")
    public ApiResponse<List<StudentOrgSummaryResponse>> getMatchedOrgs(
            @Parameter(description = "샘플링 요청 ID")
            @PathVariable Long samplingProposalId
    ) {
        return ApiResponse.success(
                samplingMatchService.getMatchedStudentOrgs(samplingProposalId)
        );
    }

    /* =========================
       학생단체 상세 조회
    ========================= */

    @GetMapping("/{samplingProposalId}/student-orgs/{orgId}")
    @Operation(summary = "3페이지 팝업: 학생단체 상세 조회")
    public ApiResponse<StudentOrgDetailResponse> getStudentOrgDetail(
            @Parameter(description = "샘플링 요청 ID")
            @PathVariable Long samplingProposalId,

            @Parameter(description = "학생단체 ID")
            @PathVariable Long orgId,

            @RequestParam int baseUnitCost,
            @RequestParam int reportOptionFee,
            @RequestParam int operationFee
    ) {
        return ApiResponse.success(
                samplingMatchService.getStudentOrgDetail(
                        samplingProposalId,
                        orgId,
                        baseUnitCost,
                        reportOptionFee,
                        operationFee
                )
        );
    }

    /* =========================
       총 예상 금액 계산
    ========================= */

    @PostMapping("/{samplingProposalId}/estimate")
    @Operation(summary = "선택한 단체들의 총 예상금액 계산")
    public ApiResponse<EstimatedTotalCostResponse> estimateTotal(
            @PathVariable Long samplingProposalId,
            @RequestParam int baseUnitCost,
            @RequestParam int reportOptionFee,
            @RequestParam int operationFee,
            @RequestBody List<Long> selectedOrgIds
    ) {
        return ApiResponse.success(
                samplingMatchService.calculateTotalEstimatedCost(
                        samplingProposalId,
                        selectedOrgIds,
                        baseUnitCost,
                        reportOptionFee,
                        operationFee
                )
        );
    }

    /* =========================
       단체별 예상 금액 계산
    ========================= */

    @GetMapping("/{samplingProposalId}/student-orgs/{orgId}/estimate")
    @Operation(summary = "단체별 예상 금액 계산")
    public ApiResponse<OrgEstimatedCostResponse> estimateOrgCost(
            @PathVariable Long samplingProposalId,
            @PathVariable Long orgId,
            @RequestParam int baseUnitCost,
            @RequestParam int reportOptionFee,
            @RequestParam int operationFee
    ) {
        return ApiResponse.success(
                samplingMatchService.calculateSingleOrgEstimatedCost(
                        samplingProposalId,
                        orgId,
                        baseUnitCost,
                        reportOptionFee,
                        operationFee
                )
        );
    }

    /* =========================
       5페이지: 매칭 제출
    ========================= */

    @PostMapping("/submit")
    @Operation(summary = "마지막: 매칭 제출")
    public ApiResponse<SamplingMatchSubmitResponse> submit(
            @AuthenticationPrincipal CustomUser user,
            @RequestBody SamplingMatchRequestDto dto
    ) {
        if (user == null || user.getUserId() == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }

        return ApiResponse.success(
                samplingMatchService.submitSamplingMatch(
                        user.getUserId(),
                        dto
                )
        );
    }
}


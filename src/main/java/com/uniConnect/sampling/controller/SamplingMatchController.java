package com.uniConnect.sampling.controller;

import com.uniConnect.global.response.ApiResponse;
import com.uniConnect.sampling.dto.*;
import com.uniConnect.sampling.service.SamplingMatchService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sampling/match")
@Tag(name = "Sampling Match", description = "샘플링 매칭 기능 API")
public class SamplingMatchController {

    private final SamplingMatchService samplingMatchService;


    @GetMapping("/{samplingRequestId}/student-orgs")
    @Operation(summary = "매칭된 학생단체 리스트 조회")
    public ApiResponse<List<StudentOrgSummaryResponse>> getMatchedOrgs(
            @Parameter(description = "샘플링 요청 ID") @PathVariable Long samplingRequestId,
            @Parameter(description = "학교명") @RequestParam(required = false) String schoolName,
            @Parameter(description = "검증 레벨") @RequestParam(required = false) Integer verificationLevel,
            @Parameter(description = "기본 단가") @RequestParam int baseUnitCost,
            @Parameter(description = "리포트 옵션 비용") @RequestParam int reportOptionFee,
            @Parameter(description = "운영비") @RequestParam int operationFee
    ) {
        return ApiResponse.success(
                samplingMatchService.getMatchedStudentOrgs(
                        samplingRequestId,
                        schoolName,
                        verificationLevel,
                        baseUnitCost,
                        reportOptionFee,
                        operationFee
                )
        );
    }


    @GetMapping("/{samplingRequestId}/student-orgs/{orgId}")
    @Operation(summary = "학생단체 상세 조회")
    public ApiResponse<StudentOrgDetailResponse> getStudentOrgDetail(
            @Parameter(description = "샘플링 요청 ID") @PathVariable Long samplingRequestId,
            @Parameter(description = "학생단체 ID") @PathVariable Long orgId,
            @Parameter(description = "기본 단가") @RequestParam int baseUnitCost,
            @Parameter(description = "리포트 옵션 비용") @RequestParam int reportOptionFee,
            @Parameter(description = "운영비") @RequestParam int operationFee
    ) {
        return ApiResponse.success(
                samplingMatchService.getStudentOrgDetail(
                        samplingRequestId,
                        orgId,
                        baseUnitCost,
                        reportOptionFee,
                        operationFee
                )
        );
    }


    @PostMapping("/{samplingRequestId}/estimate")
    @Operation(summary = "선택한 단체들의 총 예상금액 계산")
    public ApiResponse<EstimatedTotalCostResponse> estimateTotal(
            @Parameter(description = "샘플링 요청 ID") @PathVariable Long samplingRequestId,
            @Parameter(description = "기본 단가") @RequestParam int baseUnitCost,
            @Parameter(description = "리포트 옵션 비용") @RequestParam int reportOptionFee,
            @Parameter(description = "운영비") @RequestParam int operationFee,
            @RequestBody List<Long> selectedOrgIds
    ) {
        return ApiResponse.success(
                samplingMatchService.calculateTotalEstimatedCost(
                        samplingRequestId,
                        selectedOrgIds,
                        baseUnitCost,
                        reportOptionFee,
                        operationFee
                )
        );
    }

    @Operation(summary = "단체별 예상 금액 계산")
    @GetMapping("/{samplingRequestId}/student-orgs/{orgId}/estimate")
    public ApiResponse<OrgEstimatedCostResponse> estimateOrgCost(
            @PathVariable Long samplingRequestId,
            @PathVariable Long orgId,
            @RequestParam int baseUnitCost,
            @RequestParam int reportOptionFee,
            @RequestParam int operationFee
    ) {
        return ApiResponse.success(
                samplingMatchService.calculateSingleOrgEstimatedCost(
                        samplingRequestId,
                        orgId,
                        baseUnitCost,
                        reportOptionFee,
                        operationFee
                )
        );
    }


    @PostMapping("/submit")
    @Operation(summary = "매칭 제출")
    public ApiResponse<SamplingMatchSubmitResponse> submit(
            @RequestBody SamplingMatchRequestDto dto
    ) {
        return ApiResponse.success(
                samplingMatchService.submitSamplingMatch(dto)
        );
    }

}
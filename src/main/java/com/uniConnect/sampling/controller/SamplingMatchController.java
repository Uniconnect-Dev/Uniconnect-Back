package com.uniConnect.sampling.controller;

import com.uniConnect.global.response.ApiResponse;
import com.uniConnect.sampling.dto.*;
import com.uniConnect.sampling.service.SamplingMatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sampling/match")
@Tag(name = "SamplingMatch", description = "기업용 학생단체 매칭 API")
public class SamplingMatchController {

    private final SamplingMatchService matchService;

    @Operation(summary = "기업 조건 기반 학생단체 리스트 조회")
    @GetMapping
    public ApiResponse<List<StudentOrgSummaryResponse>> getMatchedOrgs(
            @RequestParam(required = false) String schoolName,
            @RequestParam(required = false) Integer verificationLevel,
            @RequestParam(defaultValue = "10000") int baseUnitCost,
            @RequestParam(defaultValue = "50000") int reportOptionFee,
            @RequestParam(defaultValue = "30000") int operationFee
    ) {
        return ApiResponse.success(
                matchService.getMatchedStudentOrgs(schoolName, verificationLevel, baseUnitCost, reportOptionFee, operationFee)
        );
    }

    @Operation(summary = "학생단체 상세정보 조회 (더 알아보기)")
    @GetMapping("/{orgId}")
    public ApiResponse<StudentOrgDetailResponse> getOrgDetail(
            @PathVariable Long orgId,
            @RequestParam(defaultValue = "10000") int baseUnitCost,
            @RequestParam(defaultValue = "50000") int reportOptionFee,
            @RequestParam(defaultValue = "30000") int operationFee
    ) {
        return ApiResponse.success(
                matchService.getStudentOrgDetail(orgId, baseUnitCost, reportOptionFee, operationFee)
        );
    }

    @Operation(summary = "선택한 학생단체 총 예상비용 안내")
    @PostMapping("/estimate")
    public ApiResponse<EstimatedTotalCostResponse> estimateTotalCost(
            @RequestBody List<Long> selectedOrgIds,
            @RequestParam(defaultValue = "10000") int baseUnitCost,
            @RequestParam(defaultValue = "50000") int reportOptionFee,
            @RequestParam(defaultValue = "30000") int operationFee
    ) {
        return ApiResponse.success(
                matchService.calculateTotalEstimatedCost(selectedOrgIds, baseUnitCost, reportOptionFee, operationFee)
        );
    }

    @Operation(summary = "매칭 요청하기", description = "기업이 타깃 키워드 선택을 완료하고 매칭 요청을 제출합니다.")
    @PostMapping("/submit")
    public ApiResponse<SamplingMatchSubmitResponse> submitMatch(@RequestBody SamplingMatchRequestDto dto) {
        SamplingMatchSubmitResponse response = matchService.submitSamplingMatch(dto);
        return ApiResponse.success(response);
    }
}

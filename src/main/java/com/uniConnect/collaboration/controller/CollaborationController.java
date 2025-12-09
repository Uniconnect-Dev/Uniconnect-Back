package com.uniConnect.collaboration.controller;

import com.uniConnect.collaboration.service.CollaborationService;
import com.uniConnect.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/collaborations")
public class CollaborationController {

    private final CollaborationService collaborationService;

    @Operation(summary = "추천 기업 도착 처리 (Admin)",
            description = "어드민이 기업 추천을 완료했을 때 호출하여 상태를 RecommendationReady로 변경합니다.")
    @PostMapping("/{id}/recommendation-ready")
    public ApiResponse<String> recommendationReady(@PathVariable Long id) {
        collaborationService.markRecommendationReady(id);
        return ApiResponse.success("추천 기업 상태로 변경됨");
    }

    @Operation(summary = "학생 → 특정 기업 매칭 요청",
            description = "추천 기업 중 원하는 기업을 선택하여 매칭을 요청합니다. 상태는 WaitingCompanyResponse로 변경됩니다.")
    @PostMapping("/{id}/request-matching")
    public ApiResponse<String> requestMatching(
            @PathVariable Long id,
            @RequestParam Long companyId
    ) {
        collaborationService.requestMatching(id, companyId);
        return ApiResponse.success("기업 매칭 요청 완료");
    }

    @Operation(summary = "계약서 발송 (Admin)",
            description = "기업이 제안을 수락한 후 어드민이 학생에게 계약서를 발송하며 상태를 ContractSent로 변경합니다.")
    @PostMapping("/{id}/send-contract")
    public ApiResponse<String> sendContract(
            @PathVariable Long id,
            @RequestParam String contractUrl
    ) {
        collaborationService.sendContract(id, contractUrl);
        return ApiResponse.success("계약서 발송됨");
    }

    @Operation(summary = "학생 서명 제출",
            description = "학생이 계약서 확인 후 전자서명을 제출하며 상태를 WaitingAdminApproval로 변경합니다.")
    @PostMapping("/{id}/sign")
    public ApiResponse<String> signContract(@PathVariable Long id) {
        collaborationService.studentSign(id);
        return ApiResponse.success("학생 서명 완료");
    }

    @Operation(summary = "어드민 계약 승인",
            description = "어드민이 학생 서명을 검토하여 최종 승인하면 상태가 WaitingReportUpload로 변경됩니다.")
    @PostMapping("/{id}/approve-contract")
    public ApiResponse<String> approveContract(@PathVariable Long id) {
        collaborationService.adminApproveContract(id);
        return ApiResponse.success("계약 승인 완료");
    }

    @Operation(summary = "리포트 업로드",
            description = "학생단체가 마케팅 활동 리포트를 업로드하며 상태가 WaitingReportApproval로 변경됩니다.")
    @PostMapping("/{id}/upload-report")
    public ApiResponse<String> uploadReport(
            @PathVariable Long id,
            @RequestParam String reportUrl
    ) {
        collaborationService.uploadReport(id, reportUrl);
        return ApiResponse.success("리포트 제출 완료");
    }

    @Operation(summary = "어드민 리포트 승인",
            description = "어드민이 제출된 리포트를 검토하고 승인하면 최종 상태가 Completed로 변경됩니다.")
    @PostMapping("/{id}/approve-report")
    public ApiResponse<String> approveReport(@PathVariable Long id) {
        collaborationService.approveReport(id);
        return ApiResponse.success("리포트 승인 완료");
    }
}

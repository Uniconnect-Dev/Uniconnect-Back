package com.uniConnect.admin.controller;

import com.uniConnect.collaboration.dto.SendContractRequest;
import com.uniConnect.collaboration.entity.Collaboration;
import com.uniConnect.collaboration.service.CollaborationAdminService;
import com.uniConnect.collaboration.dto.CollaborationSummaryResponse;
import com.uniConnect.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Admin Collaboration API", description = "관리자용 협업 관리 API (계약 / 인수증 / 리포트 / 완료)")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/collaborations")
public class AdminCollaborationController {

    private final CollaborationAdminService service;

    @Operation(summary = "협업 전체 목록 조회", description = "등록된 모든 협업 목록을 조회합니다.")
    @GetMapping
    public ApiResponse<List<CollaborationSummaryResponse>> getAll() {
        List<CollaborationSummaryResponse> list = service.findAll().stream()
                .map(CollaborationSummaryResponse::from)
                .toList();
        return ApiResponse.success(list);
    }

    @Operation(summary = "계약서 전송", description = "관리자가 특정 협업에 계약서를 전송합니다.")
    @PostMapping("/{id}/contract/send")
    public ApiResponse<Void> sendContract(
            @Parameter(description = "협업 ID", example = "1") @PathVariable Long id,
            @RequestBody SendContractRequest request) {
        service.sendContract(id, request);
        return ApiResponse.success("계약서가 전송되었습니다.", null);
    }

    @Operation(summary = "계약 승인", description = "학생단체 서명 완료 후 관리자가 계약을 승인합니다.")
    @PostMapping("/{id}/contract/approve")
    public ApiResponse<Void> approveContract(
            @Parameter(description = "협업 ID", example = "1") @PathVariable Long id) {
        service.approveContract(id);
        return ApiResponse.success("계약이 승인되었습니다.", null);
    }

    @Operation(summary = "인수증 승인", description = "학생단체가 업로드한 인수증을 승인합니다.")
    @PostMapping("/{id}/receipt/approve")
    public ApiResponse<Void> approveReceipt(
            @Parameter(description = "협업 ID", example = "1") @PathVariable Long id) {
        service.approveLatestReceipt(id);
        return ApiResponse.success("인수증이 승인되었습니다.", null);
    }

    @Operation(summary = "리포트 승인", description = "학생단체가 제출한 리포트를 승인합니다.")
    @PostMapping("/{id}/report/{reportId}/approve")
    public ApiResponse<Void> approveReport(
            @Parameter(description = "협업 ID", example = "1") @PathVariable Long id,
            @Parameter(description = "리포트 ID", example = "10") @PathVariable Long reportId) {
        service.approveReport(id, reportId);
        return ApiResponse.success("리포트가 승인되었습니다.", null);
    }
}
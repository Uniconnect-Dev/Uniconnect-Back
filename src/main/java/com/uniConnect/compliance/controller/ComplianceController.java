package com.uniConnect.compliance.controller;

import com.uniConnect.compliance.dto.ComplianceAgreementRequest;
import com.uniConnect.compliance.dto.ComplianceAgreementResponse;
import com.uniConnect.compliance.service.ComplianceService;
import com.uniConnect.global.response.ApiResponse;
import com.uniConnect.member.security.local.CustomUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/compliance")
@RequiredArgsConstructor
@Tag(name = "Compliance", description = "샘플링 규정 동의 API")
public class ComplianceController {

    private final ComplianceService complianceService;

    @PostMapping
    @Operation(summary = "샘플링 규정 동의", description = "샘플링 진행을 위한 규정 확인 및 동의 처리")
    public ResponseEntity<ApiResponse<ComplianceAgreementResponse>> agreeToCompliance(
            @AuthenticationPrincipal CustomUser customUser,
            @Valid @RequestBody ComplianceAgreementRequest request,
            HttpServletRequest httpRequest
    ) {
        ComplianceAgreementResponse response = complianceService.agreeToCompliance(
                customUser.getUsersId(),
                request,
                httpRequest
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{requestId}")
    @Operation(summary = "동의 내역 조회", description = "특정 requestId의 동의 내역 조회")
    public ResponseEntity<ApiResponse<ComplianceAgreementResponse>> getAgreement(
            @PathVariable String requestId
    ) {
        ComplianceAgreementResponse response = complianceService.getAgreementByRequestId(requestId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
package com.uniConnect.contract.controller;

import com.uniConnect.contract.dto.ContractSignatureRequest;
import com.uniConnect.contract.service.ContractSignService;
import com.uniConnect.global.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/contracts")
@Tag(name = "Contract Sign API", description = "계약서 전자서명 API")
public class ContractSignController {

    private final ContractSignService contractSignService;

    @PostMapping("/{contractId}/sign/student")
    @Operation(summary = "학생단체 계약서 서명")
    public ApiResponse<Void> signByStudent(
            @PathVariable Long contractId,
            @RequestBody ContractSignatureRequest request
    ) {
        contractSignService.signByStudent(contractId, request);
        return ApiResponse.success("학생단체 서명 완료", null);
    }

    @PostMapping("/{contractId}/sign/company")
    @Operation(summary = "기업 계약서 서명")
    public ApiResponse<Void> signByCompany(
            @PathVariable Long contractId,
            @RequestBody ContractSignatureRequest request
    ) {
        contractSignService.signByCompany(contractId, request);
        return ApiResponse.success("기업 서명 완료", null);
    }
}

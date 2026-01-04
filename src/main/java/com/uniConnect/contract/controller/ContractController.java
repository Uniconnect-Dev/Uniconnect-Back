package com.uniConnect.contract.controller;

import com.uniConnect.contract.dto.*;
import com.uniConnect.contract.service.ContractService;
import com.uniConnect.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/contracts")
@RequiredArgsConstructor
@Tag(name = "Contract API", description = "계약서 조회 및 서명 API")
public class ContractController {

    private final ContractService contractService;

    @Operation(summary = "내 계약 목록 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<List<ContractListItemDto>>> getMyContracts() {
        List<ContractListItemDto> contracts = contractService.getMyContracts();
        return ResponseEntity.ok(ApiResponse.success("내 계약 목록 조회 성공", contracts));
    }

    @Operation(summary = "계약 상세 조회")
    @GetMapping("/{contractId}")
    public ResponseEntity<ApiResponse<ContractResponseDto>> getContract(@PathVariable Long contractId) {
        ContractResponseDto contract = contractService.getContract(contractId);
        return ResponseEntity.ok(ApiResponse.success("계약 상세 조회 성공", contract));
    }

    @Operation(summary = "계약서 서명 제출")
    @PostMapping("/{contractId}/sign")
    public ResponseEntity<ApiResponse<ContractSignResponseDto>> signContract(
            @PathVariable Long contractId,
            @RequestBody ContractSignRequestDto requestDto
    ) {
        ContractSignResponseDto result = contractService.signContract(contractId, requestDto);
        return ResponseEntity.ok(ApiResponse.success("계약서 서명 완료", result));
    }

    @Operation(summary = "계약서 PDF 조회")
    @GetMapping("/{contractId}/pdf")
    public ResponseEntity<ApiResponse<ContractPdfResponseDto>> getContractPdf(@PathVariable Long contractId) {
        ContractPdfResponseDto pdf = contractService.getContractPdf(contractId);
        return ResponseEntity.ok(ApiResponse.success("계약서 PDF 조회 성공", pdf));
    }

    @Operation(summary = "인수증 조회")
    @GetMapping("/{contractId}/receipt")
    public ResponseEntity<ApiResponse<ReceiptPreviewResponseDto>> getReceipt(@PathVariable Long contractId) {
        ReceiptPreviewResponseDto receipt = contractService.getReceipt(contractId);
        return ResponseEntity.ok(ApiResponse.success("인수증 조회 성공", receipt));
    }

    @Operation(summary = "인수증 서명 제출")
    @PostMapping("/{contractId}/receipt/sign")
    public ResponseEntity<ApiResponse<ReceiptSignResponseDto>> signReceipt(
            @PathVariable Long contractId,
            @RequestBody ReceiptSignRequestDto requestDto
    ) {
        ReceiptSignResponseDto result = contractService.signReceipt(contractId, requestDto);
        return ResponseEntity.ok(ApiResponse.success("인수증 서명 완료", result));
    }

    @Operation(summary = "계약 상태 변경 (관리자용)")
    @PatchMapping("/{contractId}/status")
    public ResponseEntity<ApiResponse<ContractResponseDto>> updateStatus(
            @PathVariable Long contractId,
            @RequestBody ContractStatusUpdateRequest request
    ) {
        ContractResponseDto updated = contractService.updateStatus(contractId, request.getStatus());
        return ResponseEntity.ok(ApiResponse.success("계약 상태 변경 완료", updated));
    }
}


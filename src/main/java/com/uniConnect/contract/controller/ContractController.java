package com.uniConnect.contract.controller;

import com.uniConnect.contract.dto.*;
import com.uniConnect.contract.service.ContractService;
import com.uniConnect.member.security.local.CustomUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/contracts")
@RequiredArgsConstructor
@Tag(name = "Contract API", description = "계약서 조회 및 서명 API (JWT 인증 기반)")
public class ContractController {

    private final ContractService contractService;

    @Operation(summary = "내 계약 목록 조회 (JWT)")
    @GetMapping
    public ResponseEntity<List<ContractListItemDto>> getMyContracts(@AuthenticationPrincipal CustomUser user) {
        return ResponseEntity.ok(contractService.getMyContracts(user));
    }

    @Operation(summary = "계약 상세 조회")
    @GetMapping("/{contractId}")
    public ResponseEntity<ContractResponseDto> getContract(@PathVariable Long contractId) {
        return ResponseEntity.ok(contractService.getContract(contractId));
    }

    @Operation(summary = "계약서 서명 제출")
    @PostMapping("/{contractId}/sign")
    public ResponseEntity<ContractSignResponseDto> signContract(
            @PathVariable Long contractId,
            @RequestBody ContractSignRequestDto requestDto
    ) {
        return ResponseEntity.ok(contractService.signContract(contractId, requestDto));
    }

    @Operation(summary = "계약서 PDF 조회")
    @GetMapping("/{contractId}/pdf")
    public ResponseEntity<ContractPdfResponseDto> getContractPdf(@PathVariable Long contractId) {
        return ResponseEntity.ok(contractService.getContractPdf(contractId));
    }

    @Operation(summary = "인수증 조회")
    @GetMapping("/{contractId}/receipt")
    public ResponseEntity<ReceiptPreviewResponseDto> getReceipt(@PathVariable Long contractId) {
        return ResponseEntity.ok(contractService.getReceipt(contractId));
    }

    @Operation(summary = "인수증 서명 제출")
    @PostMapping("/{contractId}/receipt/sign")
    public ResponseEntity<ReceiptSignResponseDto> signReceipt(
            @PathVariable Long contractId,
            @RequestBody ReceiptSignRequestDto requestDto
    ) {
        return ResponseEntity.ok(contractService.signReceipt(contractId, requestDto));
    }

    @Operation(summary = "계약 상태 변경 (관리자용)")
    @PatchMapping("/{contractId}/status")
    public ResponseEntity<ContractResponseDto> updateStatus(
            @PathVariable Long contractId,
            @RequestBody Map<String, String> body
    ) {
        return ResponseEntity.ok(contractService.updateStatus(contractId, body.get("status")));
    }
}

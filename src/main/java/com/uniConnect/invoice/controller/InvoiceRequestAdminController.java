package com.uniConnect.invoice.controller;

import com.uniConnect.global.response.ApiResponse;
import com.uniConnect.invoice.dto.response.InvoiceRequestQueryDto;
import com.uniConnect.invoice.entity.InvoiceStatus;
import com.uniConnect.invoice.service.InvoiceRequestService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;

import java.util.List;

@RestController
@RequestMapping("/api/admin/invoice-requests")
@RequiredArgsConstructor
public class InvoiceRequestAdminController {

    private final InvoiceRequestService invoiceRequestService;

    /** 기업/관리자 → 발행 요청 목록 조회 */
    @Operation(
            summary = "세금계산서 발행 요청 전체 조회",
            description = "관리자 또는 기업 담당자가 모든 세금계산서 발행 요청 목록을 조회합니다."
    )
    @GetMapping
    public ResponseEntity<ApiResponse<List<InvoiceRequestQueryDto>>> getInvoiceRequests() {
        List<InvoiceRequestQueryDto> list = invoiceRequestService.getAllRequests();
        return ResponseEntity.ok(ApiResponse.success("세금계산서 발행 요청 목록 조회 성공", list));
    }

    /** 기업/관리자 → 발행 시작(IN_PROGRESS) */
    @Operation(
            summary = "세금계산서 발행 시작 처리",
            description = "요청된 세금계산서 발행을 기업/관리자가 진행 상태(IN_PROGRESS)로 변경합니다."
    )
    @PatchMapping("/{id}/start")
    public ResponseEntity<ApiResponse<String>> startInvoice(@PathVariable Long id) {

        invoiceRequestService.updateStatus(id, InvoiceStatus.InProgress);
        return ResponseEntity.ok(ApiResponse.success("발행 진행 상태로 변경됨"));
    }

    /** 기업/관리자 → 발행 완료(COMPLETED) */
    @Operation(
            summary = "세금계산서 발행 완료 처리",
            description = "기업/관리자가 세금계산서 발행을 완료했을 때 상태를 COMPLETED로 변경합니다."
    )
    @PatchMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<String>> completeInvoice(@PathVariable Long id) {

        invoiceRequestService.updateStatus(id, InvoiceStatus.Completed);
        return ResponseEntity.ok(ApiResponse.success("발행 완료 처리됨"));
    }

    /** 기업/관리자 → 발행 거절(REJECTED) */
    @Operation(
            summary = "세금계산서 발행 거절 처리",
            description = "기업/관리자가 세금계산서 발행 요청을 거절했을 때 상태를 REJECTED로 변경합니다."
    )
    @PatchMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<String>> rejectInvoice(@PathVariable Long id) {

        invoiceRequestService.updateStatus(id, InvoiceStatus.Rejected);
        return ResponseEntity.ok(ApiResponse.success("발행 요청 거절됨"));
    }
}

package com.uniConnect.payment.controller;

import com.uniConnect.payment.dto.*;
import com.uniConnect.payment.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.*;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
@Tag(name = "Payment API", description = "결제 관리 API")
public class PaymentController {
    private final PaymentService paymentService;
    private final PaymentMethodService paymentMethodService;
    private final InvoiceService invoiceService;
    private final RefundService refundService;

    // ===== 1. 결제 내역 조회 =====
    @GetMapping
    @Operation(summary = "기업의 모든 결제 내역 조회")
    public ResponseEntity<List<PaymentDto.PaymentListResponse>> getPayments() {
        Long companyId = getCurrentCompanyId();
        return ResponseEntity.ok(paymentService.getPaymentsByCompany(companyId));
    }

//    @GetMapping("/sampling/{samplingRequestId}")
//    @Operation(summary = "샘플링별 결제 내역 조회")
//    public ResponseEntity<List<PaymentDto.PaymentListResponse>> getPaymentsBySampling(@PathVariable Long samplingRequestId) {
//        return ResponseEntity.ok(paymentService.getPaymentsBySampling(samplingRequestId));
//    }

    // ===== 2. 결제 수단 관리 =====
    @PostMapping("/methods")
    @Operation(summary = "결제 수단 등록")
    public ResponseEntity<PaymentDto.PaymentMethodResponse> registerPaymentMethod(@RequestBody PaymentDto.PaymentMethodRequest request) {
        Long companyId = getCurrentCompanyId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentMethodService.registerPaymentMethod(companyId, request));
    }

    @GetMapping("/methods")
    @Operation(summary = "결제 수단 조회")
    public ResponseEntity<List<PaymentDto.PaymentMethodResponse>> getPaymentMethods() {
        Long companyId = getCurrentCompanyId();
        return ResponseEntity.ok(paymentMethodService.getPaymentMethods(companyId));
    }

    @DeleteMapping("/methods/{methodId}")
    @Operation(summary = "결제 수단 삭제")
    public ResponseEntity<Void> deletePaymentMethod(@PathVariable Long methodId) {
        Long companyId = getCurrentCompanyId();
        paymentMethodService.deletePaymentMethod(methodId, companyId);
        return ResponseEntity.noContent().build();
    }

    // ===== 3. 캠페인 결제 =====
    @PostMapping
    @Operation(summary = "결제 진행")
    public ResponseEntity<PaymentDto.PaymentListResponse> createPayment(@RequestBody PaymentDto.PaymentCreateRequest request) {
        Long companyId = getCurrentCompanyId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentService.createPayment(companyId, request));
    }

    // ===== 4. 세금계산서/영수증 발행 =====
    @PostMapping("/invoices")
    @Operation(summary = "세금계산서/영수증 발행")
    public ResponseEntity<PaymentDto.InvoiceResponse> createInvoice(@RequestBody PaymentDto.InvoiceCreateRequest request) {
        Long companyId = getCurrentCompanyId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(invoiceService.createInvoice(companyId, request));
    }

    @GetMapping("/invoices/{invoiceId}/download")
    @Operation(summary = "세금계산서/영수증 다운로드")
    public ResponseEntity<Resource> downloadInvoice(@PathVariable Long invoiceId) {
        Long companyId = getCurrentCompanyId();
        return invoiceService.downloadInvoicePdf(invoiceId, companyId);
    }

    // ===== 5. 환불 처리 =====
    @PostMapping("/refunds")
    @Operation(summary = "환불 요청")
    public ResponseEntity<PaymentDto.RefundResponse> requestRefund(@RequestBody PaymentDto.RefundRequestRequest request) {
        Long companyId = getCurrentCompanyId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(refundService.requestRefund(companyId, request));
    }

    @GetMapping("/refunds")
    @Operation(summary = "환불 요청 목록")
    public ResponseEntity<List<PaymentDto.RefundResponse>> getRefunds() {
        Long companyId = getCurrentCompanyId();
        return ResponseEntity.ok(refundService.getRefundsByCompany(companyId));
    }

    private Long getCurrentCompanyId() {
        // JWT에서 회사 ID 추출
        return SecurityUtils.getCurrentCompanyId();
    }
}
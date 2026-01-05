
package com.uniConnect.payment.controller;

import com.uniConnect.payment.dto.*;
import com.uniConnect.payment.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    /**
     * 기업의 모든 결제 내역 조회
     */
    @GetMapping("/companies/{companyId}")
    @Operation(summary = "기업의 결제 내역 조회")
    public ResponseEntity<List<PaymentDto.PaymentListResponse>> getPaymentsByCompany(
            @Parameter(description = "기업 ID", example = "1")
            @PathVariable Long companyId) {
        return ResponseEntity.ok(paymentService.getPayments(companyId));
    }

    /**
     * 학생단체의 모든 결제 내역 조회
     */
    @GetMapping("/student-orgs/{studentOrgId}")
    @Operation(summary = "학생단체의 결제 내역 조회")
    public ResponseEntity<List<PaymentDto.PaymentListResponse>> getPaymentsByStudentOrg(
            @Parameter(description = "학생단체 ID", example = "1")
            @PathVariable Long studentOrgId) {
        return ResponseEntity.ok(paymentService.getPaymentsByStudentOrg(studentOrgId));
    }

    // ===== 2. 결제 수단 관리 =====

    /**
     * 기업 결제 수단 등록
     */
    @PostMapping("/companies/{companyId}/methods")
    @Operation(summary = "기업 결제 수단 등록")
    public ResponseEntity<PaymentDto.PaymentMethodResponse> registerPaymentMethodForCompany(
            @Parameter(description = "기업 ID", example = "1")
            @PathVariable Long companyId,
            @RequestBody PaymentDto.PaymentMethodRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentMethodService.registerPaymentMethod(companyId, request));
    }

    /**
     * 기업 결제 수단 조회
     */
    @GetMapping("/companies/{companyId}/methods")
    @Operation(summary = "기업 결제 수단 조회")
    public ResponseEntity<List<PaymentDto.PaymentMethodResponse>> getPaymentMethodsForCompany(
            @Parameter(description = "기업 ID", example = "1")
            @PathVariable Long companyId) {
        return ResponseEntity.ok(paymentMethodService.getPaymentMethods(companyId));
    }

    /**
     * 기업 결제 수단 삭제
     */
    @DeleteMapping("/companies/{companyId}/methods/{methodId}")
    @Operation(summary = "기업 결제 수단 삭제")
    public ResponseEntity<Void> deletePaymentMethodForCompany(
            @Parameter(description = "기업 ID", example = "1")
            @PathVariable Long companyId,
            @Parameter(description = "결제 수단 ID", example = "1")
            @PathVariable Long methodId) {
        paymentMethodService.deletePaymentMethod(methodId, companyId);
        return ResponseEntity.noContent().build();
    }

    // ===== 3. 결제 진행 (Company) =====

    /**
     * 기업 결제 진행
     */
    @PostMapping("/companies/{companyId}")
    @Operation(summary = "기업 결제 진행")
    public ResponseEntity<PaymentDto.PaymentListResponse> createPaymentForCompany(
            @Parameter(description = "기업 ID", example = "1")
            @PathVariable Long companyId,
            @RequestBody PaymentDto.PaymentCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentService.createPayment(companyId, request));
    }

    /**
     * 기업 결제 취소
     */
    @PostMapping("/companies/{companyId}/payments/{paymentId}/cancel")
    @Operation(summary = "기업 결제 취소")
    public ResponseEntity<Void> cancelPaymentForCompany(
            @Parameter(description = "기업 ID", example = "1")
            @PathVariable Long companyId,
            @Parameter(description = "결제 ID", example = "1")
            @PathVariable Long paymentId) {
        paymentService.cancelPayment(paymentId, companyId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 기업 결제 재시도
     */
    @PostMapping("/companies/{companyId}/payments/{paymentId}/retry")
    @Operation(summary = "기업 결제 재시도")
    public ResponseEntity<PaymentDto.PaymentListResponse> retryPaymentForCompany(
            @Parameter(description = "기업 ID", example = "1")
            @PathVariable Long companyId,
            @Parameter(description = "결제 ID", example = "1")
            @PathVariable Long paymentId) {
        return ResponseEntity.ok(paymentService.retryPayment(paymentId, companyId));
    }

    // ===== 4. 결제 진행 (StudentOrg) =====

    /**
     * 학생단체 결제 진행
     */
    @PostMapping("/student-orgs/{studentOrgId}")
    @Operation(summary = "학생단체 결제 진행")
    public ResponseEntity<PaymentDto.PaymentListResponse> createPaymentForStudentOrg(
            @Parameter(description = "학생단체 ID", example = "1")
            @PathVariable Long studentOrgId,
            @RequestBody PaymentDto.PaymentCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentService.createPaymentByStudentOrg(studentOrgId, request));
    }

    /**
     * 학생단체 결제 취소
     */
    @PostMapping("/student-orgs/{studentOrgId}/payments/{paymentId}/cancel")
    @Operation(summary = "학생단체 결제 취소")
    public ResponseEntity<Void> cancelPaymentForStudentOrg(
            @Parameter(description = "학생단체 ID", example = "1")
            @PathVariable Long studentOrgId,
            @Parameter(description = "결제 ID", example = "1")
            @PathVariable Long paymentId) {
        paymentService.cancelPaymentByStudentOrg(paymentId, studentOrgId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 학생단체 결제 재시도
     */
    @PostMapping("/student-orgs/{studentOrgId}/payments/{paymentId}/retry")
    @Operation(summary = "학생단체 결제 재시도")
    public ResponseEntity<PaymentDto.PaymentListResponse> retryPaymentForStudentOrg(
            @Parameter(description = "학생단체 ID", example = "1")
            @PathVariable Long studentOrgId,
            @Parameter(description = "결제 ID", example = "1")
            @PathVariable Long paymentId) {
        return ResponseEntity.ok(paymentService.retryPaymentByStudentOrg(paymentId, studentOrgId));
    }

    // ===== 5. 세금계산서/영수증 발행 (Company) =====

    /**
     * 기업 세금계산서/영수증 발행
     */
    @PostMapping("/companies/{companyId}/invoices")
    @Operation(summary = "기업 세금계산서/영수증 발행")
    public ResponseEntity<PaymentDto.InvoiceResponse> createInvoiceForCompany(
            @Parameter(description = "기업 ID", example = "1")
            @PathVariable Long companyId,
            @RequestBody PaymentDto.InvoiceCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(invoiceService.createInvoice(companyId, request));
    }

//    /**
//     * 기업 세금계산서/영수증 다운로드
//     */
//    @GetMapping("/companies/{companyId}/invoices/{invoiceId}/download")
//    @Operation(summary = "기업 세금계산서/영수증 다운로드")
//    public ResponseEntity<Resource> downloadInvoiceForCompany(
//            @Parameter(description = "기업 ID", example = "1")
//            @PathVariable Long companyId,
//            @Parameter(description = "영수증 ID", example = "1")
//            @PathVariable Long invoiceId) {
//        return invoiceService.downloadInvoicePdf(invoiceId, companyId);
//    }

    // ===== 6. 환불 처리 =====
    /**
     * 기업 환불 요청
     */
    @PostMapping("/companies/{companyId}/refunds")
    @Operation(summary = "기업 환불 요청")
    public ResponseEntity<PaymentDto.RefundResponse> requestRefundForCompany(
            @Parameter(description = "기업 ID", example = "1")
            @PathVariable Long companyId,
            @RequestBody PaymentDto.RefundRequestRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(refundService.requestRefund(companyId, request));
    }

    /**
     * 기업 환불 요청 상세 조회
     */
    @GetMapping("/companies/{companyId}/refunds/{refundId}")
    @Operation(summary = "기업 환불 요청 상세 조회")
    public ResponseEntity<PaymentDto.RefundResponse> getRefundForCompany(
            @Parameter(description = "기업 ID", example = "1")
            @PathVariable Long companyId,
            @Parameter(description = "환불 ID", example = "1")
            @PathVariable Long refundId) {
        return ResponseEntity.ok(refundService.getRefund(refundId, companyId));
    }

    /**
     * 기업 환불 요청 목록 조회
     */
    @GetMapping("/companies/{companyId}/refunds")
    @Operation(summary = "기업 환불 요청 목록 조회")
    public ResponseEntity<List<PaymentDto.RefundResponse>> getRefundsForCompany(
            @Parameter(description = "기업 ID", example = "1")
            @PathVariable Long companyId) {
        return ResponseEntity.ok(refundService.getRefundsByCompany(companyId));
    }

    // ===== 7. 환불 처리 (StudentOrg) =====

    /**
     * 학생단체 환불 요청
     */
    @PostMapping("/student-orgs/{studentOrgId}/refunds")
    @Operation(summary = "학생단체 환불 요청")
    public ResponseEntity<PaymentDto.RefundResponse> requestRefundForStudentOrg(
            @Parameter(description = "학생단체 ID", example = "1")
            @PathVariable Long studentOrgId,
            @RequestBody PaymentDto.RefundRequestRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(refundService.requestRefundByStudentOrg(studentOrgId, request));
    }

    /**
     * 학생단체 환불 요청 상세 조회
     */
    @GetMapping("/student-orgs/{studentOrgId}/refunds/{refundId}")
    @Operation(summary = "학생단체 환불 요청 상세 조회")
    public ResponseEntity<PaymentDto.RefundResponse> getRefundForStudentOrg(
            @Parameter(description = "학생단체 ID", example = "1")
            @PathVariable Long studentOrgId,
            @Parameter(description = "환불 ID", example = "1")
            @PathVariable Long refundId) {
        return ResponseEntity.ok(refundService.getRefundByStudentOrg(refundId, studentOrgId));
    }

    /**
     * 학생단체 환불 요청 목록 조회
     */
    @GetMapping("/student-orgs/{studentOrgId}/refunds")
    @Operation(summary = "학생단체 환불 요청 목록 조회")
    public ResponseEntity<List<PaymentDto.RefundResponse>> getRefundsForStudentOrg(
            @Parameter(description = "학생단체 ID", example = "1")
            @PathVariable Long studentOrgId) {
        return ResponseEntity.ok(refundService.getRefundsByStudentOrg(studentOrgId));
    }

    // ===== 8. 환불 처리 (관리자 전용) =====

    /**
     * 환불 승인 (관리자 전용)
     */
    @PostMapping("/refunds/{refundId}/approve")
    @Operation(summary = "환불 승인 (관리자)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaymentDto.RefundResponse> approveRefund(
            @Parameter(description = "환불 ID", example = "1")
            @PathVariable Long refundId) {
        return ResponseEntity.ok(refundService.approveRefund(refundId));
    }

    /**
     * 환불 거절 (관리자 전용)
     */
    @PostMapping("/refunds/{refundId}/reject")
    @Operation(summary = "환불 거절 (관리자)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaymentDto.RefundResponse> rejectRefund(
            @Parameter(description = "환불 ID", example = "1")
            @PathVariable Long refundId,
            @RequestBody PaymentDto.RefundRejectRequest request) {
        return ResponseEntity.ok(refundService.rejectRefund(refundId, request.getRejectionReason()));
    }
}
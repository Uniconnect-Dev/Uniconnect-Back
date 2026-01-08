
package com.uniConnect.payment.controller;

import com.uniConnect.payment.enums.InvoiceStatus;
import com.uniConnect.payment.service.InvoiceRequestService;
import com.uniConnect.payment.dto.*;
import com.uniConnect.payment.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
    private final RefundService refundService;
    private final InvoiceRequestService invoiceRequestService;

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

    // ===== 2. 결제 수단 관리(기업, 학생단체) =====

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
     * 기업 결제 수단 수정
     */
    @PatchMapping("/companies/{companyId}/methods/{methodId}")
    @Operation(summary = "기업 결제 수단 수정")
    public ResponseEntity<PaymentDto.PaymentMethodResponse> patchPaymentMethodsForCompany(
            @Parameter(description = "기업 ID", example = "1")
            @PathVariable Long companyId,
            @Parameter(description = "결제 수단 ID", example = "1")
            @PathVariable Long methodId,
            @RequestBody PaymentDto.PaymentMethodRequest request) {
        return ResponseEntity.ok(paymentMethodService.updatePaymentMethod(methodId, companyId, request));
    }

    /**
     * 결제 수단 삭제
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

    @PostMapping("/studentOrg/{studentOrgId}/methods")
    @Operation(summary = "학생단체 결제 수단 등록")
    public ResponseEntity<PaymentDto.PaymentMethodResponse> registerPaymentMethodForStudentOrg(
            @Parameter(description = "학생단체 ID", example = "1")
            @PathVariable Long studentOrgId,
            @RequestBody PaymentDto.PaymentMethodRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentMethodService.registerPaymentMethodForStudentOrg(studentOrgId, request));
    }

    @GetMapping("/studentOrg/{studentOrgId}/methods")
    @Operation(summary = "학생단체 결제 수단 조회")
    public ResponseEntity<List<PaymentDto.PaymentMethodResponse>> getPaymentMethodsForStudentOrg(
            @Parameter(description = "학생단체 ID", example = "1")
            @PathVariable Long studentOrgId) {
        return ResponseEntity.ok(paymentMethodService.getPaymentMethodsByStudentOrg(studentOrgId));
    }

    @PatchMapping("/studentOrg/{studentOrgId}/methods/{methodId}")
    @Operation(summary = "학생단체 결제 수단 수정")
    public ResponseEntity<PaymentDto.PaymentMethodResponse> patchPaymentMethodsForStudentOrg(
            @Parameter(description = "학생단체 ID", example = "1")
            @PathVariable Long studentOrgId,
            @Parameter(description = "결제 수단 ID", example = "1")
            @PathVariable Long methodId,
            @RequestBody PaymentDto.PaymentMethodRequest request) {
        return ResponseEntity.ok(paymentMethodService.updatePaymentMethod(methodId, studentOrgId, request));
    }

    @DeleteMapping("/studentOrg/{studentOrgId}/methods/{methodId}")
    @Operation(summary = "학생단체 결제 수단 삭제")
    public ResponseEntity<Void> deletePaymentMethodForStudentOrg(
            @Parameter(description = "학생단체 ID", example = "1")
            @PathVariable Long studentOrgId,
            @Parameter(description = "결제 수단 ID", example = "1")
            @PathVariable Long methodId) {
        paymentMethodService.deletePaymentMethodForStudentOrg(methodId, studentOrgId);
        return ResponseEntity.noContent().build();
    }

    // ===== 3. 결제 진행 =====

    @PostMapping
    @Operation(summary = "결제 진행")
    public ResponseEntity<PaymentDto.PaymentListResponse> createPayment(
            @RequestBody PaymentDto.PaymentCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentService.createPayment(request));
    }

    @PostMapping("/cancel/{paymentId}")
    @Operation(summary = "결제 취소")
    public ResponseEntity<Void> cancelPayment(
            @Parameter(description = "결제 ID", example = "1") @PathVariable Long paymentId,
            @Parameter(description = "요청자 ID", example = "1") @RequestParam Long requesterId,
            @Parameter(description = "요청자 type", example = "COMPANY") @RequestParam String requesterType
            ) {
        paymentService.cancelPayment(paymentId, requesterId, requesterType);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/retry/{paymentId}")
    @Operation(summary = "결제 재시도")
    public ResponseEntity<PaymentDto.PaymentListResponse> retryPayment(
            @Parameter(description = "결제 ID", example = "1")
            @PathVariable Long paymentId,
            @Parameter(description = "요청자 ID", example = "1")
            @RequestParam Long requesterId,
            @Parameter(description = "요청자 타입 (COMPANY, STUDENT_ORG)", example = "COMPANY")
            @RequestParam String requesterType) {
        return ResponseEntity.ok(paymentService.retryPayment(paymentId, requesterId, requesterType));
    }

    @PostMapping("/products")
    @Operation(summary = "Product 결제 진행 (학생단체)")
    public ResponseEntity<PaymentDto.ProductPaymentResponse> createProductPayment(
            @Parameter(description = "학생단체 ID", example = "1")
            @RequestParam Long studentOrgId,
            @Valid @RequestBody PaymentDto.ProductPaymentCreateRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentService.createProductPayment(studentOrgId, request));
    }

    @PostMapping("/products/cancel/{paymentId}")
    @Operation(summary = "Product 결제 취소")
    public ResponseEntity<Void> cancelProductPayment(
            @Parameter(description = "결제 ID", example = "1")
            @PathVariable Long paymentId,
            @Parameter(description = "학생단체 ID", example = "1")
            @RequestParam Long studentOrgId) {

        paymentService.cancelProductPayment(paymentId, studentOrgId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/products/retry/{paymentId}")
    @Operation(summary = "Product 결제 재시도")
    public ResponseEntity<PaymentDto.ProductPaymentResponse> retryProductPayment(
            @Parameter(description = "결제 ID", example = "1")
            @PathVariable Long paymentId,
            @Parameter(description = "학생단체 ID", example = "1")
            @RequestParam Long studentOrgId) {

        return ResponseEntity.ok(paymentService.retryProductPayment(paymentId, studentOrgId));
    }

//     ===== 4. 세금계산서/영수증 발행 =====

    /**
     * 세금계산서/영수증 발행 요청
     */
    @PostMapping("/invoices")
    @Operation(summary = "세금계산서/영수증 발행 요청")
    public ResponseEntity<PaymentDto.InvoiceResponse> requestInvoiceIssue(
            @Parameter(description = "결제 ID", example = "1")
            @RequestParam Long paymentId,
            @Parameter(description = "요청자 ID", example = "1")
            @RequestParam Long requesterId,
            @Parameter(description = "요청자 타입 (COMPANY, STUDENT_ORG)", example = "COMPANY")
            @RequestParam String requesterType,
            @Valid @RequestBody PaymentDto.InvoiceRequestCreateRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentService.requestInvoiceIssue(paymentId, requesterId, requesterType, request));
    }

    /**
     * 세금계산서/영수증 조회 (Payment ID 기반)
     */
    @GetMapping("/{paymentId}/invoice")
    @Operation(summary = "세금계산서/영수증 조회")
    public ResponseEntity<PaymentDto.InvoiceResponse> getInvoiceByPaymentId(
            @Parameter(description = "결제 ID", example = "1")
            @PathVariable Long paymentId,
            @Parameter(description = "요청자 ID", example = "1")
            @RequestParam Long requesterId,
            @Parameter(description = "요청자 타입 (COMPANY, STUDENT_ORG)", example = "COMPANY")
            @RequestParam String requesterType) {
        return ResponseEntity.ok(paymentService.getInvoiceRequestByPaymentId(paymentId));
    }

    /**
     * 세금계산서/영수증 상세 조회
     */
    @GetMapping("/invoices/{invoiceRequestId}")
    @Operation(summary = "세금계산서/영수증 상세 조회")
    public ResponseEntity<PaymentDto.InvoiceResponse> getInvoice(
            @Parameter(description = "InvoiceRequest ID", example = "1")
            @PathVariable Long invoiceRequestId,
            @Parameter(description = "요청자 ID", example = "1")
            @RequestParam Long requesterId,
            @Parameter(description = "요청자 타입 (COMPANY, STUDENT_ORG)", example = "COMPANY")
            @RequestParam String requesterType) {
        return ResponseEntity.ok(paymentService.getInvoiceRequest(invoiceRequestId, requesterId, requesterType));
    }

    /**
     * 세금계산서/영수증 목록 조회
     */
    @GetMapping("/invoices")
    @Operation(summary = "세금계산서/영수증 목록 조회")
    public ResponseEntity<List<PaymentDto.InvoiceResponse>> getInvoices(
            @Parameter(description = "요청자 ID", example = "1")
            @RequestParam Long requesterId,
            @Parameter(description = "요청자 타입 (COMPANY, STUDENT_ORG)", example = "COMPANY")
            @RequestParam String requesterType) {
        return ResponseEntity.ok(paymentService.getInvoiceRequests(requesterId, requesterType));
    }

    /**
     * 세금계산서/영수증 상태 업데이트 (관리자)
     */
    @PostMapping("/invoices/{invoiceRequestId}/status")
    @Operation(summary = "세금계산서/영수증 상태 업데이트 (관리자)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateInvoiceStatus(
            @Parameter(description = "InvoiceRequest ID", example = "1")
            @PathVariable Long invoiceRequestId,
            @Parameter(description = "상태 (PENDING, APPROVED, REJECTED, COMPLETED)", example = "APPROVED")
            @RequestParam InvoiceStatus status) {

        invoiceRequestService.updateInvoiceRequestStatus(invoiceRequestId, status);
        return ResponseEntity.noContent().build();
    }

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
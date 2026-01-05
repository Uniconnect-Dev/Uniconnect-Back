package com.uniConnect.payment.dto;

import com.uniConnect.invoice.entity.InvoiceStatus;
import com.uniConnect.invoice.entity.InvoiceType;
import com.uniConnect.payment.enums.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;
//static inner class
public class PaymentDto {
    @Data
    @Builder
    public static class PaymentListResponse {
        private Long paymentId;
        private Integer amount;
        private PaymentStatus status;

        // PG사 거래 정보
        private String transactionId;
        private String receiptUrl;

        // 타임스탬프
        private LocalDateTime createdAt;
        private LocalDateTime completedAt;
        private LocalDateTime canceledAt;

        // 캠페인 정보
        private String campaignName;
        private Integer campaignAmount;

        // 결제 수단 정보
        private String paymentMethodType;  // 카드/계좌 등
    }

    /**
     * 결제 상세 조회 Response DTO (선택적)
     * 관리자가 결제 상세 정보를 조회할 때 사용
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentDetailResponse {
        private Long paymentId;
        private Integer amount;
        private PaymentStatus status;

        // PG사 거래 정보
        private String transactionId;
        private String receiptUrl;

        // 타임스탬프
        private LocalDateTime createdAt;
        private LocalDateTime completedAt;
        private LocalDateTime canceledAt;

        // 캠페인 정보
        private Long campaignId;
        private String campaignName;
        private Integer campaignAmount;

        // 기업 정보
        private Long companyId;
        private String companyName;

        // 결제 수단 정보
        private Long paymentMethodId;
        private String paymentMethodType;
        private String paymentMethodDisplayInfo;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentCreateRequest {
        @NotNull
        private Long campaignId;  // 샘플링 요청 ID

        @NotNull
        private Integer amount;  // 결제 금액

        @NotNull
        private Long paymentMethodId;  // 결제 수단 ID

        private String notes;  // 비고
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentMethodRequest {
        @NotNull
        private PaymentMethodType type;

        // 카드 결제용
        private String cardNumber;      // 평문 - 암호화 처리 필요
        private String expiry;          // MM/YY
        private String cvc;            // 평문 - 암호화 처리 필요

        // 계좌이체용
        private String bankName;
        private String accountNumber;   // 평문 - 암호화 처리 필요
        private String accountHolder;
    }

    @Data
    @Builder
    public static class PaymentMethodResponse {
        private Long methodId;
        private PaymentMethodType type;           // CARD, ACCOUNT, SIMPLE_PAY
        private String displayInfo;    // 마스킹된 정보 (카드 **** 1234 등)
        private String holderName;     // 카드 소유자/계좌주
    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InvoiceCreateRequest {
        @NotNull
        private Long paymentId;

        @NotNull
        private InvoiceType invoiceType;  // TAX_INVOICE, CASH_RECEIPT

        @NotNull
        private Long businessRegistrationId;

        @NotNull
        private Integer amount;

        private Integer taxAmount;  // 세금계산서인 경우

        private String notes;  // 비고

        @NotNull
        private Long samplingRequestId;  // 어느 샘플링에 대한 송장인지
    }

    @Data
    @Builder
    public static class InvoiceResponse {
        private Long invoiceId;
        private String invoiceNumber;
        private InvoiceType invoiceType;
        private InvoiceStatus status;
        private Integer amount;
        private Integer taxAmount;
        private Integer totalAmount;
        private LocalDateTime issuedAt;
        private String invoiceUrl;  // PDF URL
        private String taxOfficeConfirmationNumber;  // 국세청 확인 번호
        private String companyName;
        private String representativeName;
    }

    //user가 생성
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RefundRequestRequest {
        private Long paymentId;          // 어떤 결제에 대한 환불인지

        private String reason;            // 환불 사유

        @Min(value = 1, message = "환불 금액은 1 이상이어야 합니다")
        private Integer refundAmount;     // 환불 금액

        // 환불 계좌 정보
        private String bankName;
        private String accountNo;         // 평문 → 서버에서 암호화
        private String holderName;

        // 요청자 정보
        private String requesterName;
        private String requesterEmail;
        private String requesterPhone;

        //환불 신청일
        private LocalDateTime requestedAt;
    }

    //admin이 이미 생성한 환불req 거절
    @Data
    @Builder
    public static class RefundRejectRequest {
        private String rejectionReason;            // 환불 사유
    }

    @Data
    @Builder
    public static class RefundResponse {
        private Long refundId;
        private Long paymentId;
        private Integer refundAmount;

        // 환불 상태
        private RefundStatus refundStatus;  // PENDING, PROCESSING, COMPLETED, REJECTED

        // 사유
        private String reason;
        private String refundRejectionReason;

        // 환불 계좌 정보 (마스킹된 값 권장)
        private String bankName;
        private String maskedAccountNo;
        private String holderName;

        // 요청자 정보
        private String requesterName;
        private String requesterEmail;
        private String requesterPhone;

        // timestamp
        private LocalDateTime requestedAt;
        private LocalDateTime completedAt;
        private LocalDateTime rejectedAt;

        // 환불 거래 ID
        private String refundTransactionId;
    }

}

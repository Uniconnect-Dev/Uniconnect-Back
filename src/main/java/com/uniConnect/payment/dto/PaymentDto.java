package com.uniConnect.payment.dto;

import com.uniConnect.invoice.entity.InvoiceStatus;
import com.uniConnect.invoice.entity.InvoiceType;
import com.uniConnect.payment.enums.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
        private LocalDateTime createdAt;
        private String campaignName;  // 캠페인 명
        private Integer campaignAmount;  // 비용
        private String paymentMethodType;  // 카드/계좌 등
//        private String receiptUrl;  // 영수증 URL
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

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RefundRequest {
        private Long paymentId;          // 어떤 결제에 대한 환불인지

        private String reason;            // 환불 사유
        private Integer refundAmount;     // 환불 금액

        // 환불 계좌 정보
        private String bankName;
        private String accountNo;         // 평문 → 서버에서 암호화
        private String holderName;

        // 요청자 정보
        private String requesterName;
        private String requesterEmail;
        private String requesterPhone;
    }

    @Data
    @Builder
    public static class RefundResponse {
        private Long refundId;

        private Long paymentId;

        private String reason;
        private Integer refundAmount;

        // 환불 계좌 정보 (마스킹된 값 권장)
        private String bankName;
        private String maskedAccountNo;
        private String holderName;

        // 요청자 정보
        private String requesterName;
        private String requesterEmail;
        private String requesterPhone;

        private LocalDateTime requestedAt;
    }

    //기업 마이페이지용
    @Data
    @Builder
    public static class ListResponse {

        private Long refundId;

        private Long paymentId;

        private Integer refundAmount;
        private String bankName;
        private String holderName;

        private String requesterName;

        private LocalDateTime requestedAt;
    }

}

package com.uniConnect.payment.entity;

import com.uniConnect.common.entity.BaseEntity;
import com.uniConnect.payment.enums.InvoiceStatus;
import com.uniConnect.payment.enums.PaymentMethodType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "invoice_requests")
public class InvoiceRequest extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long invoiceRequestId;

    // === 결제 기본 정보 ===
    @Column(name = "payment_id", nullable = false)
    private Long paymentId;  // 연관된 Payment ID

    @Column(nullable = false)
    private LocalDateTime paymentDateTime;  // 결제일시

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethodType paymentMethod;  // 결제수단 (CARD, ACCOUNT, etc.)

    // === 고객 정보 ===
    @Column(nullable = false)
    private String customerName;  // 고객명: 구매하는쪽

    @Column(nullable = false)
    private String email;  // 이메일

    @Column(nullable = false)
    private String phone;  // 연락처

    // === 카드/계좌 정보 ===
    @Column(length = 50)
    private String cardNumber;  // 카드번호 (마스킹됨)

    @Column(length = 50)
    private String approvalNumber;  // 승인번호

    // === 금액 정보 ===
    @Column(nullable = false)
    private Long originalAmount;  // 최초금액

    @Column(nullable = false, columnDefinition = "DEFAULT 0")
    private Long discountAmount;  // 할인금액

    @Column(nullable = false)
    private Long taxAmount;  // 부가세 (10%)

    @Column(nullable = false)
    private Long partnershipFee;  // 제휴수수료

    @Column(nullable = false, columnDefinition = "DEFAULT 0")
    private Long additionalMarketing;  // 추가마케팅

    @Column(nullable = false)
    private Long totalAmount;  // 총결제금액

    // === 계산된 필드 (요청 시 계산) ===
    @Column(nullable = false, columnDefinition = "DEFAULT 0")
    private Long netAmount;  // 순수금액 (originalAmount - discountAmount)

    // === 사업자 정보 ===
    @Column(nullable = false)
    private String bizNumber;  // 사업자등록번호

    @Column(nullable = false)
    private String companyName;  // 회사명

    @Column(nullable = false)
    private String representativeName;  // 대표자명

    @Column(nullable = false)
    private String address;  // 사업장 주소

    @Column(nullable = false)
    private String bizType;  // 업종

    @Column(nullable = false)
    private String bizItem;  // 업태

    // === 파일 정보 ===
    @Column(columnDefinition = "text")
    private String bizCertUrl;  // 사업자등록증 파일 URL

    @Column(columnDefinition = "text")
    private String contractFileUrl;  // 계약서 파일 URL (선택)

    // === 상태 ===
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvoiceStatus status;  // PENDING, APPROVED, REJECTED, COMPLETED

    @Column(nullable = false)
    private Long userId;  // 요청자 ID
}
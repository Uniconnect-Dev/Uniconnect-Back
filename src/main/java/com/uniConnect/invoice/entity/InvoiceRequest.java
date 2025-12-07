package com.uniConnect.invoice.entity;

import com.uniConnect.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "invoice_requests")
public class InvoiceRequest extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long invoiceRequestId;

    // --- 기본 정보 ---
    @Column(nullable = false)
    private String eventName;  // 행사명

    @Column(nullable = false)
    private Long receivedAmount;  // 받은 금액(숫자)

    @Column(nullable = false, columnDefinition = "text")
    private String bizCertUrl; // 사업자등록증 파일 URL

    @Column(columnDefinition = "text")
    private String contractFileUrl; // 계약서 파일 URL (선택)

    // --- 기업 사업자 정보 (필수) ---
    @Column(nullable = false)
    private String bizNumber; // 사업자등록번호

    @Column(nullable = false)
    private String companyName;

    @Column(nullable = false)
    private String ceoName;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String bizType;

    @Column(nullable = false)
    private String contactEmail;

    @Enumerated(EnumType.STRING)
    private InvoiceType invoiceType;

    @Column(nullable = false)
    private String companyContactPhone;

    // --- 상태 ---
    @Enumerated(EnumType.STRING)
    private InvoiceStatus status;
}

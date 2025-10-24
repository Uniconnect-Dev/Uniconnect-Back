package com.uniConnect.contract.entity;

import com.uniConnect.campaign.entity.MatchingRequest;
import com.uniConnect.common.entity.BaseEntity;
import com.uniConnect.contract.enums.ContractStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "contracts")
public class Contract extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "contract_id")
    private Long contractId;

    @Column(name = "pdf_url", columnDefinition = "text")
    private String pdfUrl;

    @Column(name = "signature_file_url", columnDefinition = "text")
    private String signatureFileUrl;

    @Column(name = "company_signature_file_url", columnDefinition = "text")
    private String companySignatureFileUrl;

    @Builder.Default
    @Column(name = "company_signed", nullable = false)
    private Boolean companySigned = false;

    @Builder.Default
    @Column(name = "student_signed", nullable = false)
    private Boolean studentSigned = false;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50, nullable = false)
    private ContractStatus status = ContractStatus.PENDING_SIGNATURE;

    @Column(name = "student_signed_at")
    private LocalDateTime studentSignedAt;

    @Column(name = "company_signed_at")
    private LocalDateTime companySignedAt;

    @Column(name = "receipt_pdf_url", columnDefinition = "text")
    private String receiptPdfUrl;

    @Column(name = "receipt_signature_file_url", columnDefinition = "text")
    private String receiptSignatureFileUrl;

    @Column(name = "receipt_signed_at")
    private LocalDateTime receiptSignedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "matching_id")
    private MatchingRequest matching;

    // 연관관계 편의 메서드 (양방향)
    public void setMatching(MatchingRequest matching) {
        this.matching = matching;
        if (matching != null && matching.getContracts() != null && !matching.getContracts().contains(this)) {
            matching.getContracts().add(this);
        }
    }

    // 학생 계약서 서명
    public void markStudentSigned(String signatureUrl) {
        this.studentSigned = true;
        this.studentSignedAt = LocalDateTime.now();
        this.signatureFileUrl = signatureUrl;

        // 학생은 싸인했지만 회사는 아직
        if (Boolean.TRUE.equals(this.companySigned)) {
            this.status = ContractStatus.SIGNED;
        } else {
            this.status = ContractStatus.STUDENT_SIGNED;
        }
    }

    // 회사(운영측/admin) 계약서 서명
    public void markCompanySigned(String companySignatureUrl) {
        this.companySigned = true;
        this.companySignedAt = LocalDateTime.now();
        this.companySignatureFileUrl = companySignatureUrl;

        // 양쪽 다 싸인했으면 SIGNED
        if (Boolean.TRUE.equals(this.studentSigned)) {
            this.status = ContractStatus.SIGNED;
        }
    }

    // 인수증 서명
    public void markReceiptSigned(String receiptSignatureUrl) {
        this.receiptSignatureFileUrl = receiptSignatureUrl;
        this.receiptSignedAt = LocalDateTime.now();

        // 인수증 서명 완료되면 최종적으로 RECEIPT_SIGNED
        this.status = ContractStatus.RECEIPT_SIGNED;
    }
}

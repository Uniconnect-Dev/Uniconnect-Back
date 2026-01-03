package com.uniConnect.contract.entity;

import com.uniConnect.collaboration.entity.Collaboration;
import com.uniConnect.matching.entity.CollaborationMatchRequest;
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
    private ContractStatus status = ContractStatus.PendingSignature;

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
    @JoinColumn(name = "collaboration_id", nullable = false)
    private Collaboration collaboration;

    public void markStudentSigned(String signatureUrl) {
        this.studentSigned = true;
        this.studentSignedAt = LocalDateTime.now();
        this.signatureFileUrl = signatureUrl;

        this.status = Boolean.TRUE.equals(this.companySigned)
                ? ContractStatus.Signed
                : ContractStatus.StudentSigned;
    }

    public void markCompanySigned(String companySignatureUrl) {
        this.companySigned = true;
        this.companySignedAt = LocalDateTime.now();
        this.companySignatureFileUrl = companySignatureUrl;

        if (Boolean.TRUE.equals(this.studentSigned)) {
            this.status = ContractStatus.Signed;
        }
    }

    public void markReceiptSigned(String receiptSignatureUrl) {
        this.receiptSignatureFileUrl = receiptSignatureUrl;
        this.receiptSignedAt = LocalDateTime.now();
        this.status = ContractStatus.ReceiptSigned;
    }
}

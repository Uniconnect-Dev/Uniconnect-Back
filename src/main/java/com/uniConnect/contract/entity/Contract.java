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

    public void markStudentSigned(String signatureUrl) {
        this.studentSigned = true;
        this.studentSignedAt = LocalDateTime.now();
        this.signatureFileUrl = signatureUrl;
        updateStatusIfBothSigned();
    }

    public void markCompanySigned() {
        this.companySigned = true;
        this.companySignedAt = LocalDateTime.now();
        updateStatusIfBothSigned();
    }

    private void updateStatusIfBothSigned() {
        if (Boolean.TRUE.equals(this.studentSigned) && Boolean.TRUE.equals(this.companySigned)) {
            this.status = ContractStatus.SIGNED;
        }
    }
}

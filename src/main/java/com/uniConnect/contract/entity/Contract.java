package com.uniConnect.contract.entity;

import com.uniConnect.campaign.entity.MatchingRequest;
import com.uniConnect.contract.enums.ContractStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "contracts")
public class Contract {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "contract_id")
    private Long contractId;

    @Column(name = "pdf_url", columnDefinition = "text")
    private String pdfUrl;

    @Column(name = "company_signed")
    private Boolean companySigned;

    @Column(name = "student_signed")
    private Boolean studentSigned;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30)
    private ContractStatus status;

    @Column(name = "signed_at")
    private LocalDateTime signedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "matching_id")
    private MatchingRequest matching;
}

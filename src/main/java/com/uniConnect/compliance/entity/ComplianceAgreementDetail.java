package com.uniConnect.compliance.entity;

import com.uniConnect.common.entity.BaseEntity;
import com.uniConnect.compliance.enums.AgreementType;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "compliance_agreement_details")
public class ComplianceAgreementDetail extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "detail_id")
    private Long detailId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agreement_id", nullable = false)
    private ComplianceAgreement agreement;

    @Enumerated(EnumType.STRING)
    @Column(name = "agreement_type", nullable = false, length = 50)
    private AgreementType agreementType;

    @Column(name = "accepted", nullable = false)
    private Boolean accepted;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
}
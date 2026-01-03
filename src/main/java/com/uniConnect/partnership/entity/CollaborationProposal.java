package com.uniConnect.partnership.entity;

import com.uniConnect.company.entity.Company;
import com.uniConnect.partnership.enums.ProposalStatus;
import com.uniConnect.partnership.enums.PartnershipType;
import com.uniConnect.partnership.enums.CollaborationPeriodType;
import com.uniConnect.studentOrg.entity.StudentOrg;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import org.hibernate.annotations.Type;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "collaboration_proposals")
public class CollaborationProposal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "proposal_id")
    private Long proposalId;

    /* =========================
       관계
    ========================= */

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    /* =========================
       제휴 분류
    ========================= */

    @Enumerated(EnumType.STRING)
    @Column(name = "proposal_type", length = 30, nullable = false)
    private PartnershipType proposalType;

    /* =========================
       기업 담당자 정보
    ========================= */

    @Column(name = "contact_name", length = 100, nullable = false)
    private String contactName;

    @Column(name = "contact_phone", length = 20, nullable = false)
    private String contactPhone;

    @Column(name = "contact_email", length = 120, nullable = false)
    private String contactEmail;

    /* =========================
       제휴 정보
    ========================= */

    @Column(name = "product_or_service_name", length = 150, nullable = false)
    private String productOrServiceName;

    @Column(name = "industry", length = 50)
    private String industry;

    @Enumerated(EnumType.STRING)
    @Column(name = "period_type", length = 20, nullable = false)
    private CollaborationPeriodType periodType;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    /* =========================
       제안 내용
    ========================= */

    @Column(name = "proposal_content", columnDefinition = "text")
    private String proposalContent;

    @Column(name = "attachment_url", length = 500)
    private String attachmentUrl;

    /* =========================
       JSON 컬럼
    ========================= */

    @Type(JsonBinaryType.class)
    @Column(
            name = "collaboration_methods_json",
            columnDefinition = "json",
            nullable = false
    )
    private String collaborationMethodsJson;

    @Type(JsonBinaryType.class)
    @Column(
            name = "expected_outcomes_json",
            columnDefinition = "json"
    )
    private String expectedOutcomesJson;

    /* =========================
       동의
    ========================= */

    @Column(name = "agree_privacy", nullable = false)
    private Boolean agreePrivacy;

    @Column(name = "agree_marketing", nullable = false)
    private Boolean agreeMarketing;

    /* =========================
       상태 / 시간
    ========================= */

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private ProposalStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /* =========================
       라이프사이클
    ========================= */

    @PrePersist
    public void prePersist() {
        if (status == null) {
            status = ProposalStatus.Draft;
        }
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

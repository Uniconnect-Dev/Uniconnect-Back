package com.uniConnect.matching.entity;

import com.uniConnect.campaign.entity.Campaign;
import com.uniConnect.campaign.enums.MatchingStatus;
import com.uniConnect.sampling.enums.IndustryType;
import com.uniConnect.matching.enums.MatchSender;
import com.uniConnect.studentOrg.entity.StudentOrg;
import com.uniConnect.studentOrg.enums.*;
import com.uniConnect.company.entity.Company;
import com.uniConnect.sampling.entity.*;
import com.uniConnect.partnership.entity.CollaborationProposal;
import com.uniConnect.global.converter.EnumPascalCaseConverter;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "collaboration_match_requests")
public class CollaborationMatchRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 학생단체 → 기업 (샘플링 / 장기협업)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id")
    private Campaign campaign;

    // 기업 → 학생단체 (샘플링)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sampling_proposal_id")
    private SamplingProposal samplingProposal;

    // 기업 → 학생단체 (장기협업)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collaboration_proposal_id")
    private CollaborationProposal collaborationProposal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_org_id")
    private StudentOrg studentOrg;

    @Enumerated(EnumType.STRING)
    @Column(name = "collaboration_type", nullable = false, length = 30)
    private CollaborationType collaborationType;

    @Enumerated(EnumType.STRING)
    private MatchingStatus status; // Requested / Approved / Rejected

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MatchSender sender; // STUDENT_ORG / COMPANY

    private LocalDateTime requestedAt;
    private LocalDateTime respondedAt;

    public void approve() {
        this.status = MatchingStatus.Approved;
        this.respondedAt = LocalDateTime.now();
    }

    public void reject() {
        this.status = MatchingStatus.Rejected;
        this.respondedAt = LocalDateTime.now();
    }

    public Company getCompany() {
        if (samplingProposal != null) return samplingProposal.getCreator().getCompany();
        if (collaborationProposal != null) return collaborationProposal.getCompany();
        return null;
    }

    public StudentOrg getStudentOrg() {
        if (campaign != null) return campaign.getStudentOrg();
        if (collaborationProposal != null) return this.studentOrg;
        return null;
    }

}
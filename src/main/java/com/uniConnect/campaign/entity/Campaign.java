package com.uniConnect.campaign.entity;

import com.uniConnect.global.converter.EnumPascalCaseConverter;

import com.uniConnect.campaign.enums.CampaignStatus;
import com.uniConnect.common.entity.BaseEntity;
import com.uniConnect.company.entity.Company;
import com.uniConnect.report.entity.SamplingReport;
import com.uniConnect.studentOrg.entity.StudentOrg;
import com.uniConnect.studentOrg.enums.CollaborationType;
import com.uniConnect.studentOrg.entity.Hashtag;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.time.LocalDate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonNode;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import com.fasterxml.jackson.databind.JsonNode;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(name = "campaigns")
public class Campaign extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "campaign_id")
    private Long campaignId;

    @Column(name = "name", length = 120)
    private String name;                         // 행사명

    @Column(name = "purpose", columnDefinition = "text")
    private String purpose;

    @Column(name = "product_name", length = 100)
    private String productName;                  // 제품명

    @Column(name = "product_quantity")
    private Integer productQuantity;             // 준비 수량

    @Column(name = "start_date")
    private LocalDate startDate;                 // 시작일

    @Column(name = "end_date")
    private LocalDate endDate;                   // 종료일

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30)
    private CampaignStatus status;

    @Column(name = "brand_name", length = 100)
    private String brandName;                    // 브랜드명

    @Column(name = "location_name", length = 150)
    private String locationName;                 // 장소명

    @Column(name = "target_desc", length = 200)
    private String targetDesc;                   // 대상 (예: 대학생, 20대 여성 등)

    @Column(name = "distributed_quantity")
    private Integer distributedQuantity;         // 실제 배포 수량

    @Column(name = "labor_cost")
    private Integer laborCost;                   // 인건비

    @Column(name = "etc_cost")
    private Integer etcCost;                     // 기타비용

    @Column(name = "total_cost")
    private Integer totalCost;                   // 총비용

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_org_id")
    private StudentOrg studentOrg;

    @Column(name = "expected_participants")
    private Integer expectedParticipants;

    @Column(name = "expected_exposures")
    private Integer expectedExposures;

    @Column(name = "target_age_desc", length = 100)
    private String targetAgeDesc;

    @Column(name = "target_major_desc", length = 100)
    private String targetMajorDesc;

    @Column(name = "preferred_industry_1", length = 100)
    private String preferredIndustry1;

    @Column(name = "preferred_industry_2", length = 100)
    private String preferredIndustry2;

    @Column(name = "recommended_sampling_qty")
    private Integer recommendedSamplingQty;

    @Column(name = "booth_fee")
    private Integer boothFee;

    @Column(name = "extra_request", columnDefinition = "text")
    private String extraRequest;

    @Column(name = "proposal_file_url", columnDefinition = "text")
    private String proposalFileUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "collaboration_type", length = 30)
    private CollaborationType collaborationType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "event_programs", columnDefinition = "jsonb")
    private JsonNode eventPrograms;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "promotion_plans", columnDefinition = "jsonb")
    private JsonNode promotionPlans;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "marketing_methods", columnDefinition = "jsonb")
    private JsonNode marketingMethods;

    @OneToMany(mappedBy = "campaign", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CampaignTarget> campaignTargets;


    public List<Hashtag> getTargetKeywords() {
        if (this.campaignTargets == null) {
            return List.of();
        }

        return this.campaignTargets.stream()
                .map(CampaignTarget::getHashtag)
                .toList();
    }
}

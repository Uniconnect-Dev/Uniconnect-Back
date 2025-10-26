package com.uniConnect.campaign.entity;

import com.uniConnect.campaign.enums.CampaignStatus;
import com.uniConnect.common.entity.BaseEntity;
import com.uniConnect.company.entity.Company;
import com.uniConnect.report.entity.SamplingReport;
import com.uniConnect.studentOrg.entity.StudentOrg;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
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
    @JoinColumn(name = "company_id")
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_org_id")
    private StudentOrg studentOrg;

    @OneToOne(mappedBy = "campaign", fetch = FetchType.LAZY)
    private SamplingReport samplingReport;
}

package com.uniConnect.campaign.entity;

import com.uniConnect.campaign.enums.CampaignStatus;
import com.uniConnect.common.entity.BaseEntity;
import com.uniConnect.company.entity.Company;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "campaigns")
public class Campaign extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "campaign_id")
    private Long campaignId;

    // info
    @Column(name = "name", length = 120)
    private String name;

    @Column(name = "purpose", columnDefinition = "text")
    private String purpose;

    @Column(name = "product_name", length = 100)
    private String productName;

    @Column(name = "product_quantity")
    private Integer productQuantity;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30)
    private CampaignStatus status;

    // relation
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;
}

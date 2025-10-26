package com.uniConnect.report.entity;

import com.uniConnect.campaign.entity.Campaign;
import com.uniConnect.survey.entity.Survey;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "sampling_reports")
public class SamplingReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long reportId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id")
    private Campaign campaign;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_id")
    private Survey survey;

    // KPI 필드들
    private Integer reachCount;
    private Integer exposureCount;
    private Integer snsMentions;
    private Double preferenceScore;
    private Integer npsScore;
    private Double purchaseIntentPct;

    @Column(columnDefinition = "json")
    private String genderDistJson;

    @Column(columnDefinition = "json")
    private String ageDistJson;

    @Column(columnDefinition = "json")
    private String schoolDistJson;

    private String qualPositiveSummary;
    private String qualNegativeSummary;
    private String improvementSuggestions;
    private String roiEstimateText;
    private String benchmarkText;

    private Integer unitPrice;
    private Integer billingTotal;
    private Integer billingFee;
    private Integer billingRemaining;

    private String refundInfo;
    private String heatmapDataJson;
    private String surveyStatsJson;
    private String reportPdfUrl;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

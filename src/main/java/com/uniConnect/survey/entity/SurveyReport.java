package com.uniConnect.survey.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "survey_reports")
public class SurveyReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long reportId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_id")
    private Survey survey;

    @Column(name = "purchase_intent_pct")
    private Double purchaseIntentPct; // 구매 의향 (%)

    @Column(name = "preference_score")
    private Double preferenceScore;   // 선호도 / 호감도 지표

    @Column(name = "reach_count")
    private Integer reachCount;       // 도달 인원 수

    @Column(name = "report_pdf_url")
    private String reportPdfUrl;      // PDF 리포트 URL

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
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



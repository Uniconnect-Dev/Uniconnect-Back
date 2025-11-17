package com.uniConnect.report.entity;

import com.uniConnect.campaign.entity.Campaign;
import com.uniConnect.report.enums.SamplingReportStatus;
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

    // ─────────────────────────────
    //  행사 기본 정보
    // ─────────────────────────────

    @Column(nullable = false)
    private String eventName;    // 행사명

    @Column(nullable = false)
    private String brandName;    // 브랜드명 (기업 브랜드명)

    @Column(nullable = false)
    private String productName;  // 배포 제품명

    @Column(nullable = false)
    private String period;       // 행사 기간

    @Column(nullable = false)
    private String location;     // 배포 장소

    @Column(nullable = false)
    private String target;       // 타깃 고객층

    @Column(nullable = false)
    private Integer totalQuantity;       // 준비한 샘플 총 수량

    @Column(nullable = false)
    private Integer distributedQuantity; // 실제 배포된 수량

    // ─────────────────────────────
    //  KPI 간단 지표
    // ─────────────────────────────

    private Integer reachCount;       // 도달 인원 (배포*동행계수 기반)
    private Integer exposureCount;    // 현장 노출 인원
    private Integer snsMentions;      // SNS 언급 수

    // ─────────────────────────────
    //  KPI 상세 지표
    // ─────────────────────────────

    @Column(columnDefinition = "json")
    private String genderDistJson;    // 성별 비율 차트 데이터

    @Column(columnDefinition = "json")
    private String ageDistJson;       // 연령대 비율 차트 데이터

    @Column(columnDefinition = "json")
    private String schoolDistJson;    // 학교별 분석 결과

    @Column(columnDefinition = "json")
    private String surveyStatsJson;   // 설문 통계 요약

    @Column(columnDefinition = "json")
    private String heatmapJson;       // 행사장 Heatmap 데이터

    @Column(columnDefinition = "json")
    private String kpiChartsJson;     // 기타 KPI 차트 묶음

    /** 리포트 PDF 자동 생성 후 S3 URL */
    @Column(name = "report_pdf_url")
    private String reportPdfUrl;

    // ─────────────────────────────
    //  AI 자동 요약 / 인사이트
    // ─────────────────────────────

    @Column(columnDefinition = "TEXT")
    private String positiveSummary;        // 긍정적 반응 요약

    @Column(columnDefinition = "TEXT")
    private String negativeSummary;        // 부정적 반응 요약

    @Column(columnDefinition = "TEXT")
    private String improvementSuggestions; // 개선 제안 요약

    // ─────────────────────────────
    //  설문 기반 산출 지표
    // ─────────────────────────────

    private Double preferenceScore;     // 제품 선호도 평균
    private Integer npsScore;           // NPS 지표
    private Double purchaseIntentPct;   // 구매 의향 비율 %

    // ─────────────────────────────
    //  청구 / 비용 산출 항목
    // ─────────────────────────────

    private Integer unitPrice;          // 단가
    private Integer billingTotal;       // 총액
    private Integer billingFee;         // 수수료
    private Integer billingRemaining;   // 잔여 비용

    private String refundInfo;          // 환불 정보
    private String pricingFormula;      // 산출식 설명

    // ─────────────────────────────
    //  벤치마킹 지표 (AI 생성 가능)
    // ─────────────────────────────

    private String industryBenchmark;     // 업계 평균 대비 지표
    private String competitorBenchmark;   // 경쟁사 대비 지표
    private String roiEstimate;           // ROI 예측치

    // ─────────────────────────────
    //  리포트 승인/상태 관리
    // ─────────────────────────────

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SamplingReportStatus status; // Pending / Rejected / Approved

    // ─────────────────────────────
    //  생성/수정 시간 자동 관리
    // ─────────────────────────────

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) this.status = SamplingReportStatus.PendingApproval;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
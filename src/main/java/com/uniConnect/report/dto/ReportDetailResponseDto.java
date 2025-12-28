package com.uniConnect.report.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.time.LocalDate;

/**
 * 리포트 전체 상세 응답
 */
@Data
@Builder
public class ReportDetailResponseDto {

    private BasicInfo basicInfo;
    private Kpi kpi;
    private QualitativeInsight qualitativeInsight;
    private Finance finance;
    private Visualization visualization;
    private ExtraValue extraValue;
    private PdfInfo pdf;

    @Data
    @Builder
    public static class BasicInfo {
        private String eventTitle;
        private String brandName;
        private String productName;
        private Period period;
        private String location;
        private String targetDesc;
        private Integer plannedQuantity;
        private Integer distributedQuantity;
        private Cost cost;
    }

    @Data
    @Builder
    public static class Period {
        private LocalDate startAt;
        private LocalDate endAt;
    }

    @Data
    @Builder
    public static class Cost {
        private Integer laborCost;
        private Integer etcCost;
        private Integer totalCost;
    }

    @Data
    @Builder
    public static class Kpi {
        private Reach reach;
        private Engagement engagement;
        private Perception perception;
        private PurchaseIntent purchaseIntent;
    }

    @Data
    @Builder
    public static class Reach {
        private Integer distributedQuantity;
        private Integer reachCount;
        private Integer onSiteExposureCount;
        private Integer snsMentions;
    }

    @Data
    @Builder
    public static class Engagement {
        private Map<String, Object> genderDist; // ex {"female":70,"male":30}
        private Map<String, Object> ageDist;
        private List<Map<String, Object>> schoolDist;
    }

    @Data
    @Builder
    public static class Perception {
        private Double preferenceScore;
        private Integer npsScore;
    }

    @Data
    @Builder
    public static class PurchaseIntent {
        private Double purchaseIntentPct;
    }

    @Data
    @Builder
    public static class QualitativeInsight {
        private List<MediaItem> media;
        private List<FeedbackItem> rawFeedbackSamples;
        private String positiveSummary;
        private String negativeSummary;
        private String improvementSuggestions;
    }

    @Data
    @Builder
    public static class MediaItem {
        private String type;     // IMAGE / VIDEO
        private String url;
        private String caption;
    }

    @Data
    @Builder
    public static class FeedbackItem {
        private String quote;
        private Boolean isPositive;
    }

    @Data
    @Builder
    public static class Finance {
        private Integer unitPrice;
        private Integer billingTotal;
        private Integer billingFee;
        private Integer billingRemaining;
        private String refundInfo;
        private String pricingFormula;
    }

    @Data
    @Builder
    public static class Visualization {
        private Object kpiCharts;
        private Object surveyStats;    // 설문 통계
        private Object heatmap;        // 캠퍼스별 분포
    }

    @Data
    @Builder
    public static class ExtraValue {
        private String industryBenchmark;
        private String competitorBenchmark;
        private String roiEstimate;
    }

    @Data
    @Builder
    public static class PdfInfo {
        private String pdfUrl;
        private Boolean available;
    }
}

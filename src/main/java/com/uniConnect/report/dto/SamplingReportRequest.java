package com.uniConnect.report.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SamplingReportRequest {

    private Long campaignId;
    private Long surveyId;

    // 기본 정보
    private String eventName;
    private String brandName;
    private String productName;
    private String period;
    private String location;
    private String target;
    private Integer totalQuantity;
    private Integer distributedQuantity;

    // KPI
    private Integer reachCount;
    private Integer exposureCount;
    private Integer snsMentions;

    // 피드백
    private String feedbackText;

    private String genderDistJson;
    private String ageDistJson;
    private String schoolDistJson;
    private String surveyStatsJson;
    private String heatmapJson;
    private String kpiChartsJson;
}
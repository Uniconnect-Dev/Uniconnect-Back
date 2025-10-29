package com.uniConnect.survey.dto;

import com.uniConnect.survey.entity.SurveyReport;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SurveyReportResponseDto {
    private Long reportId;
    private Double purchaseIntentPct;
    private Double preferenceScore;
    private Integer reachCount;
    private String reportPdfUrl;

    public static SurveyReportResponseDto fromEntity(SurveyReport report) {
        return SurveyReportResponseDto.builder()
                .reportId(report.getReportId())
                .purchaseIntentPct(report.getPurchaseIntentPct())
                .preferenceScore(report.getPreferenceScore())
                .reachCount(report.getReachCount())
                .reportPdfUrl(report.getReportPdfUrl())
                .build();
    }
}

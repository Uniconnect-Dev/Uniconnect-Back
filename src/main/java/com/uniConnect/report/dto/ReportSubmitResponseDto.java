package com.uniConnect.report.dto.request;

import com.uniConnect.report.entity.SamplingReport;
import lombok.*;


@Getter @Builder
public class ReportSubmitResponseDto {
    private Long reportId;
    private Long campaignId;
    private String status;

    public static ReportSubmitResponseDto from(SamplingReport report) {
        return ReportSubmitResponseDto.builder()
                .reportId(report.getReportId())
                .campaignId(report.getCollaboration().getMatchRequest().getCampaign().getCampaignId())
                .status(report.getStatus().name())
                .build();
    }
}
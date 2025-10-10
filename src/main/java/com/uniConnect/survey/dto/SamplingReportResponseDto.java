package com.uniConnect.survey.dto;

import com.uniConnect.survey.entity.SamplingReport;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SamplingReportResponseDto {
    private Long reportId;
    private String reportTitle;
    private String content;
    private String fileUrl;
    private String status;

    public static SamplingReportResponseDto fromEntity(SamplingReport report) {
        return SamplingReportResponseDto.builder()
                .reportId(report.getReportId())
                .reportTitle(report.getReportTitle())
                .content(report.getContent())
                .fileUrl(report.getFileUrl())
                .status(report.getStatus().name())
                .build();
    }
}

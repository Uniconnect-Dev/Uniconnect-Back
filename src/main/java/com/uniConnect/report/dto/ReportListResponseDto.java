package com.uniConnect.report.dto;

import com.uniConnect.campaign.enums.CampaignStatus;
import java.time.LocalDateTime;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReportListResponseDto {

    private Long reportId;
    private String eventName;
    private String brandName;
    private String productName;
    private LocalDateTime createdAt;
    private String organizationName;
    private boolean hasPdf;
    private String status;
}

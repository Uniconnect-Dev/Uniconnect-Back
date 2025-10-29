package com.uniConnect.report.dto;

import com.uniConnect.campaign.enums.CampaignStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class ReportListResponseDto {

    private Long reportId;
    private LocalDate startDate;
    private String organizationName;
    private String productName;
    private String brandName;
    private String locationName;
    private Integer distributedQuantity;
    private Double purchaseIntentPct;
    private CampaignStatus status;
    private Boolean hasPdf;

    public ReportListResponseDto(
            Long reportId,
            LocalDate startDate,
            String organizationName,
            String productName,
            String brandName,
            String locationName,
            Integer distributedQuantity,
            Double purchaseIntentPct,
            CampaignStatus status,
            Boolean hasPdf
    ) {
        this.reportId = reportId;
        this.startDate = startDate;
        this.organizationName = organizationName;
        this.productName = productName;
        this.brandName = brandName;
        this.locationName = locationName;
        this.distributedQuantity = distributedQuantity;
        this.purchaseIntentPct = purchaseIntentPct;
        this.status = status;
        this.hasPdf = hasPdf;
    }
}

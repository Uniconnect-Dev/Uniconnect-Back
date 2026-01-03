package com.uniConnect.campaign.dto;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public class CompanyCampaignListResponse {

    private Long campaignId;

    private String campaignName;
    private Integer productQuantity;
    private LocalDate startDate;
    private LocalDate endDate;

    private String schoolName;
    private String organizationName;
    private String logoUrl;
}

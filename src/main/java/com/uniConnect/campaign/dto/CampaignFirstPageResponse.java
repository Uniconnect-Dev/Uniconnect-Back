package com.uniConnect.campaign.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CampaignFirstPageResponse {

    private String schoolName;
    private String organizationName;
}

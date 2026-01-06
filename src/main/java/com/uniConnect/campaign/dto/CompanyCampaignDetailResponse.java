package com.uniConnect.campaign.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class CompanyCampaignDetailResponse {

    // campaign
    private String name;
    private String locationName;
    private LocalDate startDate;
    private LocalDate endDate;
    private String purpose;

    private Integer expectedParticipants;
    private Integer expectedExposures;
    private String targetAgeDesc;
    private String targetMajorDesc;
    private List<String> keywords;

    private JsonNode eventPrograms;

    private String preferredIndustry1;
    private String preferredIndustry2;
    private Integer recommendedSamplingQty;
    private Integer boothFee;

    private JsonNode promotionPlans;
    private JsonNode marketingMethods;

    private String extraRequest;

    // student org
    private String schoolName;
    private String organizationName;
    private String logoUrl;
}

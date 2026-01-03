package com.uniConnect.sampling.dto;

import lombok.*;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class StudentOrgSummaryResponse {

    private Long studentOrgId;
    private String organizationName;
    private String schoolName;

    private String campaignName;
    private List<String> matchedTags;

    private Integer expectedParticipants;
    private String estimatedCostRange;
}
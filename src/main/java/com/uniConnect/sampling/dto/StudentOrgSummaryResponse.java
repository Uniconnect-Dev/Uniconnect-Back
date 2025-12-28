package com.uniConnect.sampling.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class StudentOrgSummaryResponse {
    private Long studentOrgId;
    private String organizationName;
    private String schoolName;
    private Integer expectedParticipants;
    private String estimatedCostRange;
    private String logoUrl;
}

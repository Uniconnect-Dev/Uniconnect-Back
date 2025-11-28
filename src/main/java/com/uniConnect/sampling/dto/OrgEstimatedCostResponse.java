package com.uniConnect.sampling.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class OrgEstimatedCostResponse {

    private Long studentOrgId;
    private String organizationName;

    private Integer participants;

    private Integer minEstimated;
    private Integer maxEstimated;
    private String estimatedRange;
}

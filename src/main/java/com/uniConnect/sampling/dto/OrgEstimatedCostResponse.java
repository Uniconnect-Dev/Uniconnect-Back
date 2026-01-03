package com.uniConnect.sampling.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class OrgEstimatedCostResponse {

    private Long studentOrgId;
    private String organizationName;
    private int expectedParticipants;
    private int unitCost;
    private int baseCost;
    private String estimatedCostRange;

    public static OrgEstimatedCostResponse of(
            Long studentOrgId,
            String organizationName,
            int participants,
            int unitCost,
            int baseCost
    ) {
        return new OrgEstimatedCostResponse(
                studentOrgId,
                organizationName,
                participants,
                unitCost,
                baseCost,
                String.format("%,d원 ~ %,d원",
                        baseCost - 100_000,
                        baseCost + 100_000)
        );
    }
}

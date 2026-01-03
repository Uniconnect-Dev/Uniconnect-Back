package com.uniConnect.recommendation.dto;

import lombok.*;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecommendedSamplingCompanyResponse {

    private Long companyId;
    private String brandName;
    private String logoUrl;
    private String description;
    private List<String> matchedKeywords;
    private int score;
}

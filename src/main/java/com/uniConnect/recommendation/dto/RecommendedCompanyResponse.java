package com.uniConnect.recommendation.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class RecommendedCompanyResponse {

    private Long companyId;
    private String companyName;
    private String logoUrl;
    private String description;

    private List<String> matchedTags; // 최대 2개
    private int score;
    private boolean disabled;
}
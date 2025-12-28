package com.uniConnect.curation.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CompanyCurationResponse {

    private Long companyId;
    private String brandName;
    private String logoUrl;
    private String industry;

    private double score;
    private int tagMatchCount;
    private double scaleScore;
    private boolean available;

}

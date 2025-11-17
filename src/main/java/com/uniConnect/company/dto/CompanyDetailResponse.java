package com.uniConnect.company.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CompanyDetailResponse {

    private Long companyId;
    private String brandName;
    private String logoUrl;
    private String description;
    private String website;
    private String snsUrl;
    private String industryName;
}
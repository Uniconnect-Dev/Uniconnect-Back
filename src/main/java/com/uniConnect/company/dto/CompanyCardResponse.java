package com.uniConnect.company.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CompanyCardResponse {

    private Long companyId;
    private String brandName;
    private String logoUrl;
    private String shortDescription; // 한 줄 소개
    private String industryName; // 업종

    private Boolean used;
}
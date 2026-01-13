package com.uniConnect.profile.dto;

import com.uniConnect.company.entity.Company;
import com.uniConnect.company.enums.BusinessTypeEnum;
import com.uniConnect.company.enums.IndustryType;
import lombok.*;

import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyGetResponse {

    private Long companyId;
    private String brandName;
    private String logoUrl;
    private Long mainContactId;

    // Enum으로 반환
    private IndustryType industryType;
    private String industryName;

    private BusinessTypeEnum businessType;
    private String businessTypeName;

    private String samplingPurpose;
    private LocalDate samplingStartDate;
    private LocalDate samplingEndDate;
    private String productName;
    private Integer productCount;

    public static CompanyGetResponse fromEntity(Company company) {
        return CompanyGetResponse.builder()
                .companyId(company.getCompanyId())
                .brandName(company.getBrandName())
                .logoUrl(company.getLogoUrl())
                .mainContactId(company.getMainContactId())
                .industryType(company.getIndustry() != null ? company.getIndustry().getType() : null)
                .industryName(company.getIndustry() != null ? company.getIndustry().getName() : null)
                .businessType(company.getBusinessType() != null ? company.getBusinessType().getType() : null)
                .businessTypeName(company.getBusinessType() != null ? company.getBusinessType().getName() : null)
                .samplingPurpose(company.getSamplingPurpose())
                .samplingStartDate(company.getSamplingStartDate())
                .samplingEndDate(company.getSamplingEndDate())
                .productName(company.getProductName())
                .productCount(company.getProductCount())
                .build();
    }
}
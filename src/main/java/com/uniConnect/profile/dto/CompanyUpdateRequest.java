package com.uniConnect.profile.dto;

import com.uniConnect.company.enums.BusinessTypeEnum;
import com.uniConnect.company.enums.IndustryType;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyUpdateRequest {

    private String brandName;
    private String logoUrl;
    private Long mainContactId;

    // Enum으로 변경
    private IndustryType industryType;        // 업종
    private BusinessTypeEnum businessType;    // 업태

    private String samplingPurpose;
    private LocalDate samplingStartDate;
    private LocalDate samplingEndDate;
    private String productName;
    private Integer productCount;

    // 하위 호환성을 위한 필드 (deprecated)
    @Deprecated
    private Long industryId;

    @Deprecated
    private Long businessTypeId;
}
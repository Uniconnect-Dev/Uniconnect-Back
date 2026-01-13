package com.uniConnect.profile.dto;

import com.uniConnect.company.enums.BusinessTypeEnum;
import com.uniConnect.company.enums.IndustryType;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyInitRequest {

    private String brandName;
    private String logoUrl;
    private Long mainContactId;

    // Enum으로 변경
    private IndustryType industryType;        // 업종
    private BusinessTypeEnum businessType;    // 업태

    // 하위 호환성을 위한 필드 (deprecated)
    @Deprecated
    private Long industryId;

    @Deprecated
    private Long businessTypeId;
}
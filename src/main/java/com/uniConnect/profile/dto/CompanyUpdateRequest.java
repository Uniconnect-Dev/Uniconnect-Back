package com.uniConnect.profile.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class CompanyUpdateRequest {
    private String brandName;
    private String logoUrl;
    private Long mainContactId;
    private Long industryId;
}
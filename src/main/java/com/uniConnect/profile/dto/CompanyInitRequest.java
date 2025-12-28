package com.uniConnect.profile.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class CompanyInitRequest {
    private String brandName;
    private String logoUrl;
    private Long mainContactId;
    private Long industryId;
}
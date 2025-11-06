package com.uniConnect.profile.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class CompanyGetResponse {
    private Long companyId;
    private String brandName;
    private String logoUrl;
    private Long mainContactId;
    private Long industryId;
    private String industryName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
package com.uniConnect.profile.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class StudentOrgGetResponse {
    private Long studentOrgId;
    private String schoolName;
    private String organizationName;
    private String managerName;
    private String phone;
    private String email;
    private String logoUrl;
    private Integer verificationLevel;
    private Boolean safetyFlag;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
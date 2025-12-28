package com.uniConnect.studentOrg.dto;

import com.uniConnect.studentOrg.enums.CollaborationType;
import com.uniConnect.studentOrg.enums.OrganizationType;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentOrgProfileListResponse {

    private Long studentOrgId;
    private String schoolName;
    private String organizationName;
    private String logoUrl;
    private Integer verificationLevel;
    private Boolean safetyFlag;
    private CollaborationType collaborationType;
    private OrganizationType organizationType;

    // 협업 이력 개수도 간단히
    private int collaborationCount;
}
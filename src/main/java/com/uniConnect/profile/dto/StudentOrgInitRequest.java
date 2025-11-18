package com.uniConnect.profile.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class StudentOrgInitRequest {
    private String schoolName;
    private String organizationName;
    private String managerName;
    private String phone;
    private String email;
    private String logoUrl;
}
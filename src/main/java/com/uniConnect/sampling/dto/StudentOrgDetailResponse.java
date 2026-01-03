package com.uniConnect.sampling.dto;

import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class StudentOrgDetailResponse {

    private Long studentOrgId;
    private String organizationName;
    private String schoolName;

    private String managerName;
    private String phone;
    private String email;

    private String estimatedCostRange;

}

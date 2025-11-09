package com.uniConnect.studentOrg.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentOrgCalendarResponse {

    private Long availabilityId;
    private String eventName;
    private String startDate;
    private String endDate;
    private String place;
}
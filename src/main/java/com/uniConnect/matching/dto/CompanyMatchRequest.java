package com.uniConnect.matching.dto;

import lombok.Getter;
import java.time.LocalDate;
import java.util.List;

@Getter
public class CompanyMatchRequest {
    private String eventTitle;
    private String collaborationType;
    private LocalDate desiredDate;
    private String industry;
    private java.util.List<Long> targetStudentOrgIds;
}
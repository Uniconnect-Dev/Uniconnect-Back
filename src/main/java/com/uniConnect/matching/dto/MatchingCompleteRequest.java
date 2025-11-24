package com.uniConnect.matching.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class MatchingCompleteRequest {
    private List<Long> selectedCompanyIds;
    private String eventTitle;
    private LocalDate desiredDate;
    private String industry;
    private String collaborationType;
}

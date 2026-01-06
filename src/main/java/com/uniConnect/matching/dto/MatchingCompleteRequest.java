package com.uniConnect.matching.dto;

import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Data
@Getter
@Setter
public class MatchingCompleteRequest {
    List<Long> companyIds;
    private List<Long> selectedCompanyIds;
}

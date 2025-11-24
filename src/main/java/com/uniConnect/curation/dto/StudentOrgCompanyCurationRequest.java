package com.uniConnect.curation.dto;

import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentOrgCompanyCurationRequest {

    private LocalDate samplingStartDate;
    private LocalDate samplingEndDate;

    private Integer requestedQuantity;
    private List<String> qTags;
}

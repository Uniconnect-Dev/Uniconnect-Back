package com.uniConnect.contract.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class MatchingFilterRequest {

    private String period;

    private LocalDate startDate;
    private LocalDate endDate;

    private String collaborationType;

    private String contractStatus;
}

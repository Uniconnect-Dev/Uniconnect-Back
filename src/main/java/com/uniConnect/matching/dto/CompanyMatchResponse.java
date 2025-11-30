package com.uniConnect.matching.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CompanyMatchResponse {
    private int total;
    private String message;
}
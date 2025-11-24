package com.uniConnect.matching.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MatchingCompleteResponse {
    private int total;
    private String message;
}
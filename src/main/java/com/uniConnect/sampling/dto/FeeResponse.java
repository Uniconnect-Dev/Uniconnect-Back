package com.uniConnect.sampling.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FeeResponse {
    private final int perSamplingFee;
    private final int deposit;
    private final int totalEstimatedAmount;
}
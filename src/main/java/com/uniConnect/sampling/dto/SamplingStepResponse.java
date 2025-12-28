package com.uniConnect.sampling.dto.response;

import com.uniConnect.sampling.enums.SamplingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SamplingStepResponse {
    private Long samplingRequestId;
    private SamplingStatus status;
    private String message;
}

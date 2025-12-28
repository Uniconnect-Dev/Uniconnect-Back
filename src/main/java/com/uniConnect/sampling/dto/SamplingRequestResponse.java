package com.uniConnect.sampling.dto.response;

import com.uniConnect.sampling.enums.SamplingStatus;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SamplingRequestResponse {
    private Long samplingRequestId;
    private SamplingStatus status;
    private String requesterUsername;
}
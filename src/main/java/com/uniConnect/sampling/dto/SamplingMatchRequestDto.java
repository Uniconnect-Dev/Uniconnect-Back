package com.uniConnect.sampling.dto;

import java.util.List;

public record SamplingMatchRequestDto(
        Long samplingProposalId,
        List<Long> campaignIds
) {}

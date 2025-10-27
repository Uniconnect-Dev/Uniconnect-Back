package com.uniConnect.sampling.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class SamplingTargetResponseDto {
    private Long samplingRequestId;
    private List<String> category1Keywords;
    private List<String> category2Keywords;
    private List<String> category3Keywords;

    private List<SamplingTargetOptionDto> category1Options;
    private List<SamplingTargetOptionDto> category2Options;
    private List<SamplingTargetOptionDto> category3Options;
}

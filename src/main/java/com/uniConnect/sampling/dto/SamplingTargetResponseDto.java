package com.uniConnect.sampling.dto;

import lombok.*;

import java.util.Map;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SamplingTargetResponseDto {
    private Long requestId;

    private List<String> category1Keywords;
    private List<String> category2Keywords;
    private List<String> category3Keywords;
    private List<String> industryOfficialKeywords;

    private Map<String, List<SamplingTargetOptionDto>> category1Options;
    private Map<String, List<SamplingTargetOptionDto>> category2Options;
    private Map<String, List<SamplingTargetOptionDto>> category3Options;
    private Map<String, List<SamplingTargetOptionDto>> industryOfficialOptions;
}
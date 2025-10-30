package com.uniConnect.sampling.dto;

import lombok.*;

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

    private List<SamplingTargetOptionDto> category1Options;
    private List<SamplingTargetOptionDto> category2Options;
    private List<SamplingTargetOptionDto> category3Options;
    private List<SamplingTargetOptionDto> industryOfficialOptions;
}
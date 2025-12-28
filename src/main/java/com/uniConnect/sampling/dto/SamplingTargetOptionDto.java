package com.uniConnect.sampling.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SamplingTargetOptionDto {
    private Long keywordId;
    private String label;
    private String description;
    private String subCategory;
}

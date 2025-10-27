package com.uniConnect.sampling.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SamplingTargetRequestDto {
    private List<Long> category1KeywordIds;
    private List<Long> category2KeywordIds;
    private List<Long> category3KeywordIds;
}

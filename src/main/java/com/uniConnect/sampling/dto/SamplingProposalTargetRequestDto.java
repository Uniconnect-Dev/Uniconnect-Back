package com.uniConnect.sampling.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class SamplingProposalTargetRequestDto {

    private Long samplingProposalId;

    // 기본 정보
    private List<Long> basicInfoKeywordIds;

    // 관심사
    private List<Long> lifestyleKeywordIds;

    // 성격/행사 성격
    private List<Long> eventNatureKeywordIds;

    // 커스텀 키워드 (카테고리 = ETC)
    private List<String> customKeywords;
}

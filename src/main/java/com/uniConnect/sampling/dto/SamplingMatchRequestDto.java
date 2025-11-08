package com.uniConnect.sampling.dto;

import lombok.*;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class SamplingMatchRequestDto {
    private Long samplingRequestId;         // 어떤 요청에 대한 매칭인지
    private List<Long> selectedKeywordIds;  // 선택된 타깃 키워드 ID 리스트
}

package com.uniConnect.sampling.dto;

import com.uniConnect.sampling.enums.SamplingStatus;
import lombok.*;

import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class SamplingMatchSubmitResponse {

    private Long samplingRequestId;
    private SamplingStatus status;
    private String orgName;      // 요청한 단체명 (sampling_request에 있는 값)
    private String schoolName;   // 학교명
    private List<SelectedTargetDto> selectedTargets;

    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor @Builder
    public static class SelectedTargetDto {
        private Long selectionId;
        private Long keywordId;
        private String label;           // 예: #이화여대
        private String category;        // 예: UNIVERSITY, REGION ...
    }
}
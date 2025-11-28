package com.uniConnect.sampling.dto;

import com.uniConnect.sampling.entity.SamplingRequest;
import com.uniConnect.sampling.enums.SamplingStatus;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SamplingMatchSubmitResponse {

    private Long samplingRequestId;
    private SamplingStatus status;
    private String orgName;
    private String schoolName;
    private List<SelectedTargetDto> selectedTargets;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SelectedTargetDto {
        private Long selectionId;
        private Long keywordId;
        private String label;
        private String category;
    }

    public static SamplingMatchSubmitResponse from(SamplingRequest request) {

        List<SelectedTargetDto> targetDtos = request.getSelections().stream()
                .map(sel -> SelectedTargetDto.builder()
                        .selectionId(sel.getSelectionId())
                        .keywordId(sel.getTargetKeyword().getTargetKeywordId())
                        .label(sel.getSelectedLabel())
                        .category(sel.getCategory().name())
                        .build()
                )
                .toList();

        return SamplingMatchSubmitResponse.builder()
                .samplingRequestId(request.getSamplingRequestId())
                .status(request.getStatus())
                .orgName(request.getOrgName())
                .schoolName(request.getSchoolName())
                .selectedTargets(targetDtos)
                .build();
    }
}

package com.uniConnect.survey.dto;

import com.uniConnect.survey.entity.Survey;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SurveyResponseDto {
    private Long surveyId;
    private String title;
    private String description;
    private String status;

    public static SurveyResponseDto fromEntity(Survey survey) {
        return SurveyResponseDto.builder()
                .surveyId(survey.getSurveyId())
                .title(survey.getTitle())
                .description(survey.getDescription())
                .status(survey.getStatus().name())
                .build();
    }
}

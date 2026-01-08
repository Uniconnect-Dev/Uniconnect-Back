package com.uniConnect.survey.dto;

import com.uniConnect.survey.entity.SurveyResponse;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SurveyAnswerResponseDto {

    private Long responseId;
    private Long surveyId;
    private String surveyTitle;
    private String summaryText;
    private LocalDateTime createdAt;

    public static SurveyAnswerResponseDto fromEntity(SurveyResponse response) {
        return SurveyAnswerResponseDto.builder()
                .responseId(response.getResponseId())
                .surveyId(response.getSurvey().getSurveyId())
                .surveyTitle(response.getSurvey().getTitle())
                .summaryText(response.getSummaryText())
                .createdAt(response.getCreatedAt())
                .build();
    }
}

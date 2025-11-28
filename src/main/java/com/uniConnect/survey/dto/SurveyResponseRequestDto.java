package com.uniConnect.survey.dto;

import com.uniConnect.survey.entity.Survey;
import com.uniConnect.survey.entity.SurveyStatus;
import lombok.*;

@Getter
@Setter
public class SurveyResponseRequestDto {
    private String fileUrl;
    private String summaryText;
}

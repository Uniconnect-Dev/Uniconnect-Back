package com.uniConnect.survey.dto;

import com.uniConnect.survey.entity.Survey;
import com.uniConnect.survey.entity.SurveyStatus;
import lombok.*;

@Getter
@Setter
public class SurveyUpdateRequestDto {
    private String title;
    private String description;
    private String externalLink;
}

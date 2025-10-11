package com.uniConnect.survey.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SurveyRequestDto {
    private Long studentOrgId;
    private String title;
    private String description;
    private String externalLink;
}
package com.uniConnect.survey.dto;

import com.uniConnect.survey.entity.Survey;
import com.uniConnect.survey.entity.SurveyStatus;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SurveyResponseDto {
    private Long surveyId;
    private Long studentOrgId;
    private String title;
    private String description;
    private String externalLink;
    private SurveyStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static SurveyResponseDto fromEntity(Survey s) {
        return SurveyResponseDto.builder()
                .surveyId(s.getSurveyId())
                .studentOrgId(s.getStudentOrg().getStudentOrgId())
                .title(s.getTitle())
                .description(s.getDescription())
                .externalLink(s.getExternalLink())
                .status(s.getStatus())
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .build();
    }
}

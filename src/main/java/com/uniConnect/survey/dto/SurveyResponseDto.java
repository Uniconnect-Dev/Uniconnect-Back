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

        Long orgId = null;
        if (s.getStudentOrg() != null) {
            orgId = s.getStudentOrg().getStudentOrgId();
        }

        return SurveyResponseDto.builder()
                .surveyId(s.getSurveyId())
                .studentOrgId(orgId)
                .title(s.getTitle())
                .description(s.getDescription())
                .externalLink(s.getExternalLink())
                .status(s.getStatus())
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .build();
    }
}

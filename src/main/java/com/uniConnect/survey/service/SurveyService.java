package com.uniConnect.survey.service;

import com.uniConnect.survey.dto.*;
import com.uniConnect.survey.entity.*;
import com.uniConnect.survey.repository.*;
import com.uniConnect.global.exception.CustomException;
import com.uniConnect.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SurveyService {

    private final SurveyRepository surveyRepository;
    private final SamplingReportRepository reportRepository;

    public SurveyResponseDto createSurvey(SurveyRequestDto requestDto) {
        Survey survey = Survey.builder()
                .title(requestDto.getTitle())
                .description(requestDto.getDescription())
                .build();

        Survey saved = surveyRepository.save(survey);
        return SurveyResponseDto.fromEntity(saved);
    }

    public List<SurveyResponseDto> getSurveys(Long studentOrgId) {
        return surveyRepository.findByStudentOrg_StudentOrgId(studentOrgId)
                .stream()
                .map(SurveyResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    public SamplingReportResponseDto createReport(Long surveyId, SamplingReportRequestDto dto) {
        Survey survey = surveyRepository.findById(surveyId)
                .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));

        SamplingReport report = SamplingReport.builder()
                .survey(survey)
                .reportTitle(dto.getReportTitle())
                .content(dto.getContent())
                .fileUrl(dto.getFileUrl())
                .build();

        SamplingReport saved = reportRepository.save(report);
        return SamplingReportResponseDto.fromEntity(saved);
    }

    public SamplingReportResponseDto getReport(Long surveyId) {
        SamplingReport report = reportRepository.findBySurvey_SurveyId(surveyId)
                .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));

        return SamplingReportResponseDto.fromEntity(report);
    }
}

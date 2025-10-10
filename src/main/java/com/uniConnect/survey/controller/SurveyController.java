package com.uniConnect.survey.controller;

import com.uniConnect.survey.dto.*;
import com.uniConnect.survey.service.SurveyService;
import com.uniConnect.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/surveys")
public class SurveyController {

    private final SurveyService surveyService;

    @PostMapping
    public ApiResponse<SurveyResponseDto> createSurvey(@RequestBody SurveyRequestDto request) {
        return ApiResponse.success("설문 생성 성공", surveyService.createSurvey(request));
    }

    @GetMapping("/{studentOrgId}")
    public ApiResponse<List<SurveyResponseDto>> getSurveys(@PathVariable Long studentOrgId) {
        return ApiResponse.success("설문 조회 성공", surveyService.getSurveys(studentOrgId));
    }

    @PostMapping("/{surveyId}/sampling")
    public ApiResponse<SamplingReportResponseDto> createSamplingReport(
            @PathVariable Long surveyId,
            @RequestBody SamplingReportRequestDto request
    ) {
        return ApiResponse.success("샘플링 보고서 생성 성공", surveyService.createReport(surveyId, request));
    }

    @GetMapping("/{surveyId}/sampling")
    public ApiResponse<SamplingReportResponseDto> getSamplingReport(@PathVariable Long surveyId) {
        return ApiResponse.success("샘플링 보고서 조회 성공", surveyService.getReport(surveyId));
    }
}

package com.uniConnect.report.controller;

import com.uniConnect.global.response.ApiResponse;
import com.uniConnect.report.dto.request.*;
import com.uniConnect.report.entity.SamplingReport;
import com.uniConnect.member.entity.User;
import com.uniConnect.member.security.local.CustomUser;
import com.uniConnect.global.exception.CustomException;
import com.uniConnect.global.exception.ErrorCode;
import com.uniConnect.report.service.SamplingReportService;
import com.uniConnect.member.repository.LocalCredentialRepository;
import com.uniConnect.member.repository.UserRepository;
import com.uniConnect.member.security.local.JwtUtil;
import com.uniConnect.studentOrg.entity.StudentOrg;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import jakarta.validation.Valid;
import com.fasterxml.jackson.databind.ObjectMapper;


import java.util.*;

@Tag(name = "Sampling Report API", description = "학생단체 리포트 제출 및 관리 API")
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Slf4j
public class SamplingReportController {

    private final SamplingReportService reportService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final LocalCredentialRepository localCredentialRepository;
    private final ObjectMapper objectMapper;

    @Operation(
            summary = "학생단체 리포트 제출",
            description = "로그인한 학생단체 소속 사용자 계정 기반으로 리포트를 제출합니다. 사진/영상 첨부 가능."
    )
    @PostMapping(value = "/submit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<?> submitReport(
            @AuthenticationPrincipal CustomUser principal,
            @RequestPart("request") String requestJson,
            @RequestPart(value = "mediaFiles", required = false) List<MultipartFile> mediaFiles
    ) {

        if (principal == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED, "로그인이 필요합니다.");
        }

        User user = userRepository.findById(principal.getUserId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        StudentOrg studentOrg = user.getStudentOrg();
        if (studentOrg == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED, "학생단체 계정만 리포트를 제출할 수 있습니다.");
        }

        SamplingReportRequest request;
        try {
            request = objectMapper.readValue(requestJson, SamplingReportRequest.class);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE,
                    "request JSON 파싱 실패: " + e.getMessage());
        }

        validateJsonField(request.getGenderDistJson(), "genderDistJson");
        validateJsonField(request.getAgeDistJson(), "ageDistJson");
        validateJsonField(request.getSchoolDistJson(), "schoolDistJson");
        validateJsonField(request.getSurveyStatsJson(), "surveyStatsJson");
        validateJsonField(request.getHeatmapJson(), "heatmapJson");
        validateJsonField(request.getKpiChartsJson(), "kpiChartsJson");

        SamplingReport saved = reportService.submitReport(
                request,
                mediaFiles,
                studentOrg.getStudentOrgId()
        );

        return ApiResponse.success(
                "리포트가 성공적으로 제출되었습니다.",
                ReportSubmitResponseDto.from(saved)
        );
    }

    private void validateJsonField(String value, String fieldName) {
        if (value == null || value.isBlank() || value.equalsIgnoreCase("null")) return;

        try {
            objectMapper.readTree(value);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE,
                    fieldName + " 필드가 유효한 JSON 형식이 아닙니다: " + value);
        }
    }

    @Operation(summary = "어드민 리포트 승인", description = "리포트 승인 시 캠페인 상태가 완료로 변경됩니다.")
    @PostMapping("/{reportId}/approve")
    public ApiResponse<String> approveReport(@PathVariable Long reportId) {
        reportService.approveReport(reportId);
        return ApiResponse.success("리포트가 승인되었습니다.", null);
    }

    @Operation(summary = "어드민 리포트 거절", description = "리포트를 거절합니다.")
    @PostMapping("/{reportId}/reject")
    public ApiResponse<String> rejectReport(@PathVariable Long reportId) {
        reportService.rejectReport(reportId);
        return ApiResponse.success("리포트가 거절되었습니다.", null);
    }
}
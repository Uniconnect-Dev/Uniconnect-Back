package com.uniConnect.survey.controller;

import com.uniConnect.survey.dto.*;
import com.uniConnect.survey.entity.*;
import com.uniConnect.survey.util.CsvUtil;
import com.uniConnect.survey.util.ExcelUtil;
import com.uniConnect.survey.service.SurveyService;
import com.uniConnect.member.security.local.CustomUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.net.URL;
import java.io.InputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.io.ByteArrayInputStream;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/surveys")
@Tag(name = "Survey API", description = "설문 등록 및 조회 관련 API")
public class SurveyController {

    private Long getLoginUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Object principal = auth.getPrincipal();

        if (principal instanceof CustomUser customUser) {
            return customUser.getUserId();
        }

        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인 정보가 올바르지 않습니다.");
    }

    private final SurveyService surveyService;

    @Operation(summary = "설문 등록", description = "JWT 인증을 사용해 로그인한 단체가 새 설문을 등록합니다.")
    @PostMapping
    public ResponseEntity<?> createSurvey(@RequestBody SurveyRequestDto dto) {

        SurveyResponseDto created = surveyService.createSurveyByJwt(dto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of(
                        "surveyId", created.getSurveyId(),
                        "message", "설문이 성공적으로 생성되었습니다."
                ));
    }

    @Operation(summary = "전체 설문 조회(어드민용)")
    @GetMapping
    public ResponseEntity<List<SurveyResponseDto>> getAllSurveys() {
        return ResponseEntity.ok(surveyService.getAllSurveys());
    }

    @Operation(summary = "내 학생단체 전체 설문 목록 조회", description = "로그인한 단체의 설문 목록을 조회합니다.")
    @GetMapping("/student-org")
    public ResponseEntity<List<SurveyResponseDto>> getMySurveys() {

        Long userId = getLoginUserId();
        Long orgId = surveyService.findOrgIdByUserId(userId);

        return ResponseEntity.ok(surveyService.getSurveysByOrg(orgId));
    }

    @Operation(
            summary = "내 기업 전체 설문 응답 목록 조회",
            description = "JWT 인증을 통해 로그인한 기업이 보유한 모든 설문의 응답을 조회합니다."
    )
    @GetMapping("/company")
    public ResponseEntity<List<SurveyAnswerResponseDto>> getAllSurveyResponsesByCompany() {

        Long userId = getLoginUserId();

        return ResponseEntity.ok(
                surveyService.getAllResponsesByCompany(userId)
        );
    }

//    @Operation(summary = "특정 단체의 설문 목록 조회")
//    @GetMapping("/org/{orgId}")
//    public ResponseEntity<List<SurveyResponseDto>> getSurveysByOrg(@PathVariable Long orgId) {
//        return ResponseEntity.ok(surveyService.getSurveysByOrg(orgId));
//    }

    @Operation(summary = "설문 상세 조회")
    @GetMapping("/{id}")
    public ResponseEntity<SurveyResponseDto> getSurvey(@PathVariable Long id) {
        return ResponseEntity.ok(surveyService.getSurvey(id));
    }

    @Operation(summary = "설문 외부 링크 이동")
    @GetMapping("/{id}/link")
    public ResponseEntity<Void> openSurveyLink(@PathVariable Long id) {
        String link = surveyService.getExternalLink(id);
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, link)
                .build();
    }

    @Operation(summary = "설문 상태 변경")
    @PatchMapping("/{id}/status")
    public ResponseEntity<SurveyResponseDto> updateSurveyStatus(
            @PathVariable Long id,
            @RequestParam SurveyStatus status
    ) {
        return ResponseEntity.ok(surveyService.updateStatus(id, status));
    }

    @Operation(summary = "설문 수정")
    @PatchMapping("/{id}")
    public ResponseEntity<SurveyResponseDto> updateSurvey(
            @PathVariable Long id,
            @RequestBody SurveyUpdateRequestDto dto
    ) {
        return ResponseEntity.ok(surveyService.updateSurvey(id, dto));
    }

    @Operation(summary = "학생단체 설문 응답")
    @PostMapping("/{id}/responses")
    public ResponseEntity<?> addSurveyResponse(
            @PathVariable Long id,
            @RequestBody SurveyResponseRequestDto dto
    ) {
        surveyService.addResponse(id, dto);
        return ResponseEntity.ok(Map.of("message", "응답이 제출되었습니다."));
    }

    @Operation(summary = "기업이 설문 응답 조회")
    @GetMapping("/{id}/responses")
    public ResponseEntity<?> getResponses(
            @PathVariable Long id,
            @RequestParam(defaultValue = "false") boolean masking
    ) {
        return ResponseEntity.ok(surveyService.getResponses(id, masking));
    }

    @Operation(summary = "CSV 다운로드")
    @GetMapping("/{id}/download/csv")
    public ResponseEntity<Resource> downloadCsv(@PathVariable Long id) {
        List<SurveyResponse> responses = surveyService.getResponses(id, false);
        ByteArrayInputStream bis = CsvUtil.responsesToCsv(responses);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=survey_responses.csv");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(new InputStreamResource(bis));
    }


    @Operation(summary = "XLSX 다운로드")
    @GetMapping("/{id}/download/xlsx")
    public ResponseEntity<Resource> downloadXlsx(@PathVariable Long id) {
        List<SurveyResponse> responses = surveyService.getResponses(id, false);
        ByteArrayInputStream bis = ExcelUtil.responsesToExcel(responses);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=survey_responses.xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(bis));
    }

    @Operation(summary = "PDF 다운로드")
    @GetMapping("/{id}/download/pdf")
    public ResponseEntity<Resource> downloadPdf(@PathVariable Long id) {

        Survey survey = surveyService.getSurveyEntity(id);

        SurveyReport report = survey.getSurveyReport();
        if (report == null || report.getReportPdfUrl() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "PDF 리포트가 존재하지 않습니다.");
        }

        String pdfUrl = report.getReportPdfUrl();

        try {
            URL url = new URL(pdfUrl);
            InputStream inputStream = url.openStream();

            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "inline; filename=survey_report.pdf");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(new InputStreamResource(inputStream));

        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "PDF 파일을 불러올 수 없습니다.");
        }
    }
}

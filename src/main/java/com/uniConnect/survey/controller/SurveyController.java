package com.uniConnect.survey.controller;

import com.uniConnect.survey.dto.SurveyRequestDto;
import com.uniConnect.survey.dto.SurveyResponseDto;
import com.uniConnect.survey.entity.SurveyStatus;
import com.uniConnect.survey.service.SurveyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/surveys")
@Tag(name = "Survey API", description = "설문 등록 및 조회 관련 API (JWT 인증 기반)")
public class SurveyController {

    private final SurveyService surveyService;

    @Operation(summary = "설문 등록", description = "JWT 인증을 사용해 로그인한 단체가 새 설문을 등록합니다.")
    @PostMapping
    public ResponseEntity<?> createSurvey(@RequestBody SurveyRequestDto dto) {
        // JWT sub에 들어있는 loginId를 principal로 가져옴
        SurveyResponseDto created = surveyService.createSurveyByJwt(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("surveyId", created.getSurveyId(), "message", "설문이 등록되었습니다."));
    }

    @Operation(summary = "전체 설문 조회 (관리자용)")
    @GetMapping
    public ResponseEntity<List<SurveyResponseDto>> getAllSurveys() {
        return ResponseEntity.ok(surveyService.getAllSurveys());
    }

    @Operation(summary = "내 단체 설문 목록 조회", description = "JWT 인증을 통해 로그인한 단체의 설문 목록을 조회합니다.")
    @GetMapping("/me")
    public ResponseEntity<List<SurveyResponseDto>> getMySurveys() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String loginId = (String) authentication.getPrincipal();

        Long orgId = surveyService.findOrgIdByLoginId(loginId);
        return ResponseEntity.ok(surveyService.getSurveysByOrg(orgId));
    }

    @Operation(summary = "특정 단체의 설문 목록 조회")
    @GetMapping("/org/{orgId}")
    public ResponseEntity<List<SurveyResponseDto>> getSurveysByOrg(@PathVariable Long orgId) {
        return ResponseEntity.ok(surveyService.getSurveysByOrg(orgId));
    }

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
}

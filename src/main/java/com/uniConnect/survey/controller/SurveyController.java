package com.uniConnect.survey.controller;

import com.uniConnect.survey.dto.SurveyRequestDto;
import com.uniConnect.survey.dto.SurveyResponseDto;
import com.uniConnect.survey.entity.SurveyStatus;
import com.uniConnect.survey.service.SurveyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/surveys")
public class SurveyController {

    private final SurveyService surveyService;

    //설문 등록
    @PostMapping
    public ResponseEntity<?> createSurvey(@RequestBody SurveyRequestDto dto) {
        SurveyResponseDto created = surveyService.createSurvey(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("surveyId", created.getSurveyId(), "message", "설문이 등록되었습니다."));
    }

    //전체 설문 조회 (관리자용)
    @GetMapping
    public ResponseEntity<List<SurveyResponseDto>> getAllSurveys() {
        return ResponseEntity.ok(surveyService.getAllSurveys());
    }

    // 특정 단체 설문 조회
    @GetMapping("/org/{orgId}")
    public ResponseEntity<List<SurveyResponseDto>> getSurveysByOrg(@PathVariable Long orgId) {
        return ResponseEntity.ok(surveyService.getSurveysByOrg(orgId));
    }

    //설문 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<SurveyResponseDto> getSurvey(@PathVariable Long id) {
        return ResponseEntity.ok(surveyService.getSurvey(id));
    }

    // 구글폼 링크로 이동
    @GetMapping("/{id}/link")
    public ResponseEntity<Void> openSurveyLink(@PathVariable Long id) {
        String link = surveyService.getExternalLink(id);
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, link)
                .build();
    }

    //설문 상태 변경 (PENDING → ACTIVE → CLOSED)
    @PatchMapping("/{id}/status")
    public ResponseEntity<SurveyResponseDto> updateSurveyStatus(
            @PathVariable Long id,
            @RequestParam SurveyStatus status
    ) {
        return ResponseEntity.ok(surveyService.updateStatus(id, status));
    }
}


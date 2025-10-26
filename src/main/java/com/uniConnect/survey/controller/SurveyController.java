package com.uniConnect.survey.controller;

import com.uniConnect.member.security.local.CustomUser;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/surveys")
@Tag(name = "Survey API", description = "설문 등록 및 조회 관련 API")
public class SurveyController {

    private final SurveyService surveyService;

    /**
     * 설문 등록 API
     */
    @Operation(summary = "설문 등록", description = "JWT 인증을 사용해 로그인한 단체가 새 설문을 등록합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "설문 등록 성공"),
            @ApiResponse(responseCode = "400", description = "요청 형식 오류")
    })
    @PostMapping
    public ResponseEntity<?> createSurvey(
            @AuthenticationPrincipal CustomUser user,
            @RequestBody SurveyRequestDto dto
    ) {
        SurveyResponseDto created = surveyService.createSurveyByJwt(user, dto);
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
    public ResponseEntity<List<SurveyResponseDto>> getMySurveys(@AuthenticationPrincipal CustomUser user) {
        return ResponseEntity.ok(surveyService.getSurveysByOrg(user.getUsersId()));
    }

    @Operation(
            summary = "특정 단체의 설문 목록 조회",
            description = "단체 ID(orgId)에 해당하는 설문 목록을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = SurveyResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "해당 단체의 설문이 없습니다.", content = @Content)
    })
    @GetMapping("/org/{orgId}")
    public ResponseEntity<List<SurveyResponseDto>> getSurveysByOrg(
            @Parameter(description = "단체 ID", example = "1")
            @PathVariable Long orgId
    ) {
        return ResponseEntity.ok(surveyService.getSurveysByOrg(orgId));
    }

    @Operation(summary = "설문 상세 조회")
    @GetMapping("/{id}")
    public ResponseEntity<SurveyResponseDto> getSurvey(@PathVariable Long id) {
        return ResponseEntity.ok(surveyService.getSurvey(id));
    }

    /**
     * 설문 외부 링크(구글폼 등)로 이동
     */
    @Operation(
            summary = "설문 외부 링크 이동",
            description = "해당 설문의 외부 링크(예: 구글폼)로 리디렉션합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "302", description = "링크로 리디렉션"),
            @ApiResponse(responseCode = "404", description = "해당 설문을 찾을 수 없습니다.", content = @Content)
    })
    @GetMapping("/{id}/link")
    public ResponseEntity<Void> openSurveyLink(
            @Parameter(description = "설문 ID", example = "1")
            @PathVariable Long id
    ) {
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


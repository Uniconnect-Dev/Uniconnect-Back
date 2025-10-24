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
    @Operation(
            summary = "설문 등록",
            description = "학생 단체가 새 설문을 등록합니다. 제목, 설명, 외부 링크(구글폼 등)를 입력받아 생성합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "설문 등록 성공",
                    content = @Content(schema = @Schema(example = "{\"surveyId\": 1, \"message\": \"설문이 등록되었습니다.\"}"))),
            @ApiResponse(responseCode = "400", description = "요청 형식 오류", content = @Content)
    })
    @PostMapping
    public ResponseEntity<?> createSurvey(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "설문 등록 요청 DTO",
                    required = true,
                    content = @Content(schema = @Schema(implementation = SurveyRequestDto.class))
            )
            @RequestBody SurveyRequestDto dto
    ) {
        SurveyResponseDto created = surveyService.createSurvey(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("surveyId", created.getSurveyId(), "message", "설문이 등록되었습니다."));
    }

    /**
     * 전체 설문 조회 (관리자용)
     */
    @Operation(
            summary = "전체 설문 조회 (관리자용)",
            description = "모든 설문 목록을 조회합니다. 관리자 권한이 필요할 수 있습니다."
    )
    @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(schema = @Schema(implementation = SurveyResponseDto.class)))
    @GetMapping
    public ResponseEntity<List<SurveyResponseDto>> getAllSurveys() {
        return ResponseEntity.ok(surveyService.getAllSurveys());
    }

    /**
     * 특정 단체의 설문 목록 조회
     */
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

    /**
     * 설문 상세 조회
     */
    @Operation(
            summary = "설문 상세 조회",
            description = "설문 ID로 단일 설문 상세 정보를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = SurveyResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "설문을 찾을 수 없습니다.", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<SurveyResponseDto> getSurvey(
            @Parameter(description = "설문 ID", example = "1")
            @PathVariable Long id
    ) {
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

    /**
     * 설문 상태 변경 API
     */
    @Operation(
            summary = "설문 상태 변경",
            description = "설문 상태를 변경합니다. 가능한 값: PENDING → ACTIVE → CLOSED"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "상태 변경 성공",
                    content = @Content(schema = @Schema(implementation = SurveyResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 상태 값", content = @Content),
            @ApiResponse(responseCode = "404", description = "설문을 찾을 수 없습니다.", content = @Content)
    })
    @PatchMapping("/{id}/status")
    public ResponseEntity<SurveyResponseDto> updateSurveyStatus(
            @Parameter(description = "설문 ID", example = "1")
            @PathVariable Long id,
            @Parameter(description = "변경할 설문 상태", example = "ACTIVE")
            @RequestParam SurveyStatus status
    ) {
        return ResponseEntity.ok(surveyService.updateStatus(id, status));
    }
}


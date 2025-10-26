package com.uniConnect.report.controller;

import com.uniConnect.report.dto.ReportDetailResponseDto;
import com.uniConnect.report.dto.ReportListResponseDto;
import com.uniConnect.report.dto.ReportPdfMetaDto;
import com.uniConnect.report.service.ReportPdfService;
import com.uniConnect.report.service.ReportQueryService;
import com.uniConnect.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse as ApiResponseAnnotation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Tag(name = "Report API", description = "리포트 조회 및 PDF 다운로드 관련 API")
public class ReportController {

    private final ReportQueryService reportQueryService;
    private final ReportPdfService reportPdfService;

    /**
     * 리포트 목록 조회
     */
    @Operation(
            summary = "리포트 목록 조회",
            description = """
                    리포트 목록을 조회합니다.
                    필터 조건:
                    - studentOrgId: 학생 단체 ID
                    - productName: 제품명 키워드
                    - dateFrom, dateTo: 날짜 범위
                    - page, size: 페이지네이션
                    """
    )
    @ApiResponses({
            @ApiResponseAnnotation(responseCode = "200", description = "리포트 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = ReportListResponseDto.class))),
            @ApiResponseAnnotation(responseCode = "400", description = "잘못된 요청 파라미터"),
            @ApiResponseAnnotation(responseCode = "500", description = "서버 오류")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<ReportListResponseDto>>> getReportList(
            @Parameter(description = "학생 단체 ID", example = "1")
            @RequestParam(required = false) Long studentOrgId,

            @Parameter(description = "제품명 키워드", example = "콜드브루")
            @RequestParam(required = false) String productName,

            @Parameter(description = "시작 날짜 (YYYY-MM-DD)", example = "2025-01-01")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,

            @Parameter(description = "종료 날짜 (YYYY-MM-DD)", example = "2025-12-31")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,

            @Parameter(description = "페이지 번호", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "페이지 크기", example = "20")
            @RequestParam(defaultValue = "20") int size
    ) {
        List<ReportListResponseDto> result = reportQueryService.getReportList(
                studentOrgId,
                productName,
                dateFrom,
                dateTo,
                page,
                size
        );

        return ResponseEntity.ok(ApiResponse.success("리포트 목록 조회 성공", result));
    }

    /**
     * 리포트 상세 조회
     */
    @Operation(
            summary = "리포트 상세 조회",
            description = "단일 리포트의 KPI, 인사이트, 재무 정보, 벤치마크 등의 상세 정보를 조회합니다."
    )
    @ApiResponses({
            @ApiResponseAnnotation(responseCode = "200", description = "리포트 상세 조회 성공",
                    content = @Content(schema = @Schema(implementation = ReportDetailResponseDto.class))),
            @ApiResponseAnnotation(responseCode = "404", description = "리포트를 찾을 수 없음"),
            @ApiResponseAnnotation(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponseAnnotation(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/{reportId}")
    public ResponseEntity<ReportDetailResponseDto> getReportDetail(
            @Parameter(description = "리포트 ID", required = true, example = "10")
            @PathVariable Long reportId
    ) {
        ReportDetailResponseDto detail = reportQueryService.getReportDetail(reportId);
        return ResponseEntity.ok(detail);
    }

    /**
     * PDF 메타 정보 조회
     */
    @Operation(
            summary = "리포트 PDF 메타 정보 조회",
            description = """
                    리포트 PDF 파일의 메타 정보를 조회합니다.
                    프론트엔드는 반환된 pdfUrl을 사용하여
                    - iframe으로 PDF 미리보기 표시
                    - 다운로드 버튼 노출
                    등의 UI를 구현할 수 있습니다.
                    """
    )
    @ApiResponses({
            @ApiResponseAnnotation(responseCode = "200", description = "PDF 메타 조회 성공",
                    content = @Content(schema = @Schema(implementation = ReportPdfMetaDto.class))),
            @ApiResponseAnnotation(responseCode = "404", description = "리포트 또는 PDF 파일 없음"),
            @ApiResponseAnnotation(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponseAnnotation(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/{reportId}/pdf")
    public ResponseEntity<ReportPdfMetaDto> getPdfMeta(
            @Parameter(description = "리포트 ID", required = true, example = "10")
            @PathVariable Long reportId
    ) {
        ReportPdfMetaDto meta = reportPdfService.getPdfMeta(reportId);
        return ResponseEntity.ok(meta);
    }

    /**
     * PDF 다운로드
     */
    @Operation(
            summary = "리포트 PDF 다운로드",
            description = "리포트 PDF 파일을 바이너리 형태로 다운로드합니다."
    )
    @ApiResponses({
            @ApiResponseAnnotation(responseCode = "200", description = "PDF 다운로드 성공",
                    content = @Content(mediaType = "application/pdf")),
            @ApiResponseAnnotation(responseCode = "404", description = "PDF 파일을 찾을 수 없음"),
            @ApiResponseAnnotation(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponseAnnotation(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/{reportId}/pdf/download")
    public ResponseEntity<Resource> downloadPdf(
            @Parameter(description = "리포트 ID", required = true, example = "10")
            @PathVariable Long reportId
    ) {
        return reportPdfService.downloadPdf(reportId);
    }
}

package com.uniConnect.report.controller;

import com.uniConnect.global.response.ApiResponse;
import com.uniConnect.report.dto.ReportDetailResponseDto;
import com.uniConnect.report.dto.ReportListResponseDto;
import com.uniConnect.report.dto.ReportPdfMetaDto;
import com.uniConnect.report.service.ReportPdfService;
import com.uniConnect.report.service.ReportQueryService;
import com.uniConnect.member.security.local.CustomUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Tag(name = "Sampling Report API", description = "기업이 캠페인 기반 샘플링 KPI 리포트를 조회하는 기능")
public class ReportController {

    private final ReportQueryService reportQueryService;
    private final ReportPdfService reportPdfService;

    @Operation(summary = "기업 리포트 목록 조회",
            description = """
               기업이 진행한 캠페인의 샘플링 KPI 리포트 목록을 조회합니다.
               - 기간 필터(dateFrom, dateTo)
               - 캠페인별 리포트 요약 정보 반환
               - PDF 생성 여부 포함
               """)
    @PreAuthorize("hasRole('Company')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<ReportListResponseDto>>> getReportList(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUser user = (CustomUser) authentication.getPrincipal();
        Long userId = Long.valueOf(user.getUserId());

        List<ReportListResponseDto> result =
                reportQueryService.getReportList(userId, dateFrom, dateTo, page, size);

        return ResponseEntity.ok(ApiResponse.success("리포트 목록 조회 성공", result));
    }

    @Operation(summary = "기업 리포트 상세 조회",
            description = """
               기업이 캠페인 단위 리포트 상세 KPI 데이터를 조회합니다.
               - 기본 정보 (행사명, 기간, 장소, 비용)
               - KPI 지표 (도달, 노출, 참여, 인식, 구매 의향 등)
               - 정성적 분석(요약, 피드백)
               - 추가 가치 지표 및 차트 데이터
               """)
    @PreAuthorize("hasRole('Company')")
    @GetMapping("/{reportId}")
    public ResponseEntity<ApiResponse<ReportDetailResponseDto>> getReportDetail(
            @PathVariable Long reportId
    ) {
        ReportDetailResponseDto detail = reportQueryService.getReportDetail(reportId);
        return ResponseEntity.ok(ApiResponse.success("리포트 상세 조회 성공", detail));
    }

    @Operation(summary = "리포트 PDF 파일 메타 정보 조회",
            description = "리포트 PDF URL, 생성 여부 등을 조회합니다.")
    @PreAuthorize("hasRole('Company')")
    @GetMapping("/{reportId}/pdf")
    public ResponseEntity<ApiResponse<ReportPdfMetaDto>> getPdfMeta(
            @PathVariable Long reportId
    ) {
        return ResponseEntity.ok(ApiResponse.success(reportPdfService.getPdfMeta(reportId)));
    }

    @Operation(summary = "리포트 PDF 다운로드",
            description = "생성된 리포트 PDF 파일을 다운로드합니다.")
    @PreAuthorize("hasRole('Company')")
    @GetMapping("/{reportId}/pdf/download")
    public ResponseEntity<Resource> downloadPdf(
            @PathVariable Long reportId
    ) {
        return reportPdfService.downloadPdf(reportId);
    }
}
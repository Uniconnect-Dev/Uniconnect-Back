package com.uniConnect.report.controller;

import com.uniConnect.member.security.local.CustomUser;
import com.uniConnect.report.dto.ReportDetailResponseDto;
import com.uniConnect.report.dto.ReportListResponseDto;
import com.uniConnect.report.dto.ReportPdfMetaDto;
import com.uniConnect.report.service.ReportPdfService;
import com.uniConnect.report.service.ReportQueryService;
import com.uniConnect.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Tag(name = "Report API", description = "리포트 조회 및 PDF 다운로드 (JWT 인증 기반)")
public class ReportController {

    private final ReportQueryService reportQueryService;
    private final ReportPdfService reportPdfService;

    @Operation(summary = "리포트 목록 조회 (JWT)")
    @GetMapping
    public ResponseEntity<ApiResponse<List<ReportListResponseDto>>> getReportList(
            @AuthenticationPrincipal CustomUser user,
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        List<ReportListResponseDto> result = reportQueryService.getReportList(
                user.getUsersId(), productName, dateFrom, dateTo, page, size
        );
        return ResponseEntity.ok(ApiResponse.success("리포트 목록 조회 성공", result));
    }

    @Operation(summary = "리포트 상세 조회 (JWT)")
    @GetMapping("/{reportId}")
    public ResponseEntity<ReportDetailResponseDto> getReportDetail(
            @PathVariable Long reportId
    ) {
        ReportDetailResponseDto detail = reportQueryService.getReportDetail(reportId);
        return ResponseEntity.ok(detail);
    }

    @Operation(summary = "리포트 PDF 메타 조회 (JWT)")
    @GetMapping("/{reportId}/pdf")
    public ResponseEntity<ReportPdfMetaDto> getPdfMeta(@PathVariable Long reportId) {
        return ResponseEntity.ok(reportPdfService.getPdfMeta(reportId));
    }

    @Operation(summary = "리포트 PDF 다운로드 (JWT)")
    @GetMapping("/{reportId}/pdf/download")
    public ResponseEntity<Resource> downloadPdf(@PathVariable Long reportId) {
        return reportPdfService.downloadPdf(reportId);
    }
}


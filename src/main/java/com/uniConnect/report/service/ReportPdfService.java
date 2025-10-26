package com.uniConnect.report.service;

import com.uniConnect.report.dto.ReportPdfMetaDto;
import com.uniConnect.report.entity.SamplingReport;
import com.uniConnect.report.repository.SamplingReportRepository;
import com.uniConnect.s3.S3FileService;
import com.uniConnect.s3.S3Props;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * PDF 리포트 메타 조회 + S3 다운로드 처리
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportPdfService {

    private final SamplingReportRepository samplingReportRepository;
    private final ReportAccessValidator accessValidator;

    private final S3FileService s3FileService;
    private final S3Props s3Props;

    /**
     * /api/reports/{reportId}/pdf
     * PDF 메타 조회 (뷰어용)
     */
    public ReportPdfMetaDto getPdfMeta(Long reportId) {
        accessValidator.validateCanAccessReport(reportId);

        SamplingReport report = samplingReportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Report not found: " + reportId));

        String pdfUrl = report.getReportPdfUrl();
        String fileName = buildFileName(report);

        return new ReportPdfMetaDto(reportId, pdfUrl, fileName);
    }

    /**
     * /api/reports/{reportId}/pdf/download
     * 실제 S3에서 PDF 파일 다운로드 스트림 반환
     */
    public ResponseEntity<Resource> downloadPdf(Long reportId) {
        accessValidator.validateCanAccessReport(reportId);

        SamplingReport report = samplingReportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Report not found: " + reportId));

        if (report.getReportPdfUrl() == null) {
            throw new IllegalStateException("PDF 파일이 등록되지 않았습니다.");
        }

        String key = extractKeyFromUrl(report.getReportPdfUrl());

        Resource resource;
        try {
            resource = s3FileService.download(s3Props.bucket(), key);
        } catch (IOException e) {
            throw new RuntimeException("S3에서 PDF 파일을 다운로드하는 중 오류가 발생했습니다: " + e.getMessage(), e);
        }

        String fileName = buildFileName(report);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(
                ContentDisposition.attachment()
                        .filename(fileName, StandardCharsets.UTF_8)
                        .build()
        );

        return ResponseEntity.ok()
                .headers(headers)
                .body(resource);
    }

    private String buildFileName(SamplingReport report) {
        return "report_" + report.getReportId() + ".pdf";
    }

    private String extractKeyFromUrl(String pdfUrl) {
        if (pdfUrl == null) return null;
        int idx = pdfUrl.indexOf(".amazonaws.com/");
        if (idx == -1) return pdfUrl;
        return pdfUrl.substring(idx + ".amazonaws.com/".length());
    }
}

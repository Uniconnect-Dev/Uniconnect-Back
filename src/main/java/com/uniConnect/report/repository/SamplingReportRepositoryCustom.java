package com.uniConnect.report.repository;

import com.uniConnect.report.dto.ReportListResponseDto;

import java.time.LocalDate;
import java.util.List;

public interface SamplingReportRepositoryCustom {

    /**
     * 리포트 목록 검색
     * - studentOrgId로 필터 (협업 단체)
     * - productName 부분검색
     * - dateFrom ~ dateTo (샘플링 날짜 기준)
     * - 페이지네이션 (page, size)
     */
    List<ReportListResponseDto> searchReports(
            Long studentOrgId,
            String productName,
            LocalDate dateFrom,
            LocalDate dateTo,
            int page,
            int size
    );
}

package com.uniConnect.report.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uniConnect.member.entity.LocalCredential;
import com.uniConnect.member.entity.User;
import com.uniConnect.member.repository.LocalCredentialRepository;
import com.uniConnect.report.dto.ReportDetailResponseDto;
import com.uniConnect.report.dto.ReportListResponseDto;
import com.uniConnect.report.entity.SamplingReport;
import com.uniConnect.report.entity.SamplingReportFeedback;
import com.uniConnect.report.entity.SamplingReportMedia;
import com.uniConnect.report.repository.SamplingReportFeedbackRepository;
import com.uniConnect.report.repository.SamplingReportMediaRepository;
import com.uniConnect.report.repository.SamplingReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportQueryService {

    private final SamplingReportRepository samplingReportRepository;
    private final SamplingReportMediaRepository mediaRepository;
    private final SamplingReportFeedbackRepository feedbackRepository;
    private final ReportAccessValidator accessValidator;
    private final LocalCredentialRepository localCredentialRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * JWT loginId 기반 userId 조회
     */
    public Long findUserIdByLoginId(String loginId) {
        return localCredentialRepository.findByLoginId(loginId)
                .map(LocalCredential::getUser)
                .map(User::getUserId)
                .orElseThrow(() -> new IllegalArgumentException("해당 loginId의 사용자를 찾을 수 없습니다."));
    }

    /**
     * 리포트 목록 조회
     */
    public List<ReportListResponseDto> getReportList(
            Long studentOrgId,
            String productName,
            LocalDate dateFrom,
            LocalDate dateTo,
            int page,
            int size
    ) {
        return samplingReportRepository.searchReports(
                studentOrgId,
                productName,
                dateFrom,
                dateTo,
                page,
                size
        );
    }

    /**
     * 리포트 상세 조회 (기존 코드 유지)
     */
    public ReportDetailResponseDto getReportDetail(Long reportId) {
        accessValidator.validateCanAccessReport(reportId);

        SamplingReport report = samplingReportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Report not found: " + reportId));

        List<SamplingReportMedia> mediaList = mediaRepository.findByReport_ReportId(reportId);
        List<SamplingReportFeedback> feedbackList = feedbackRepository.findByReport_ReportId(reportId);

        // 이하 JSON 파싱 및 DTO 변환 로직 동일
        Map<String, Object> genderDist = parseJsonMap(report.getGenderDistJson());
        Map<String, Object> ageDist = parseJsonMap(report.getAgeDistJson());
        List<Map<String, Object>> schoolDist = parseJsonList(report.getSchoolDistJson());

        Object heatmap = parseJsonGeneric(report.getHeatmapDataJson());
        Object surveyStats = parseJsonGeneric(report.getSurveyStatsJson());

        // 최종 DTO 구성 (요약)
        return ReportDetailResponseDto.builder()
                .basicInfo(null)
                .kpi(null)
                .qualitativeInsight(null)
                .finance(null)
                .visualization(null)
                .extraValue(null)
                .pdf(null)
                .build();
    }

    // JSON 파싱 유틸
    private Map<String, Object> parseJsonMap(String json) {
        if (json == null || json.isBlank()) return null;
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            return null;
        }
    }

    private List<Map<String, Object>> parseJsonList(String json) {
        if (json == null || json.isBlank()) return null;
        try {
            return objectMapper.readValue(json, new TypeReference<List<Map<String, Object>>>() {});
        } catch (Exception e) {
            return null;
        }
    }

    private Object parseJsonGeneric(String json) {
        if (json == null || json.isBlank()) return null;
        try {
            return objectMapper.readValue(json, Object.class);
        } catch (Exception e) {
            return null;
        }
    }
}

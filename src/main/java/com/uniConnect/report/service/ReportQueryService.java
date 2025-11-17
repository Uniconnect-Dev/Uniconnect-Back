package com.uniConnect.report.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import lombok.RequiredArgsConstructor;

import com.uniConnect.report.dto.ReportListResponseDto;
import com.uniConnect.report.dto.ReportDetailResponseDto;
import com.uniConnect.report.entity.SamplingReport;
import com.uniConnect.report.enums.SamplingReportStatus;
import com.uniConnect.report.repository.SamplingReportRepository;
import com.uniConnect.member.entity.User;
import com.uniConnect.member.repository.UserRepository;


@Service
@RequiredArgsConstructor
public class ReportQueryService {

    private final UserRepository userRepository;
    private final SamplingReportRepository samplingReportRepository;
    private final ObjectMapper objectMapper;

    private Map<String, Object> parseJsonMap(String json) {
        if (json == null) return new HashMap<>();
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    private List<Map<String, Object>> parseJsonList(String json) {
        if (json == null) return new ArrayList<>();
        try {
            return objectMapper.readValue(json, new TypeReference<List<Map<String, Object>>>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private Object parseJsonGeneric(String json) {
        if (json == null) return null;
        try {
            return objectMapper.readValue(json, Object.class);
        } catch (Exception e) {
            return null;
        }
    }

    public Long findUserIdByLoginId(String loginId) {
        return userRepository.findByUsername(loginId)
                .map(User::getUserId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
    }

    public List<ReportListResponseDto> getReportList(
            Long userId,
            String status,
            LocalDate startDate,
            LocalDate endDate,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(page, size);

        SamplingReportStatus statusEnum = null;
        if (status != null && !status.isBlank()) {
            statusEnum = SamplingReportStatus.valueOf(status);
        }

        LocalDateTime start = (startDate != null) ? startDate.atStartOfDay() : null;
        LocalDateTime end = (endDate != null) ? endDate.atTime(23, 59, 59) : null;

        Page<SamplingReport> reports = samplingReportRepository.findReports(
                userId, statusEnum, start, end, pageable
        );

        return reports.stream()
                .map(r -> ReportListResponseDto.builder()
                        .reportId(r.getReportId())
                        .eventName(r.getEventName())
                        .brandName(r.getBrandName())
                        .productName(r.getProductName())
                        .createdAt(r.getCreatedAt())
                        .status(r.getStatus().name())
                        .build()
                )
                .toList();
    }

    public ReportDetailResponseDto getReportDetail(Long reportId) {

        SamplingReport report = samplingReportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("리포트를 찾을 수 없습니다."));

        return ReportDetailResponseDto.builder()
                .basicInfo(
                        ReportDetailResponseDto.BasicInfo.builder()
                                .eventTitle(report.getEventName())
                                .brandName(report.getBrandName())
                                .productName(report.getProductName())
                                .period(
                                        ReportDetailResponseDto.Period.builder()
                                                .startAt(report.getPeriod())  // 엔티티에 맞춤
                                                .endAt(null)
                                                .build()
                                )
                                .location(report.getLocation())
                                .targetDesc(report.getTarget())
                                .plannedQuantity(report.getTotalQuantity())
                                .distributedQuantity(report.getDistributedQuantity())
                                .cost(
                                        ReportDetailResponseDto.Cost.builder()
                                                .laborCost(null)
                                                .etcCost(null)
                                                .totalCost(report.getBillingTotal())
                                                .build()
                                )
                                .build()
                )
                .kpi(
                        ReportDetailResponseDto.Kpi.builder()
                                .reach(
                                        ReportDetailResponseDto.Reach.builder()
                                                .distributedQuantity(report.getDistributedQuantity())
                                                .reachCount(report.getReachCount())
                                                .onSiteExposureCount(report.getExposureCount())
                                                .snsMentions(report.getSnsMentions())
                                                .build()
                                )
                                .engagement(
                                        ReportDetailResponseDto.Engagement.builder()
                                                .genderDist(parseJsonMap(report.getGenderDistJson()))
                                                .ageDist(parseJsonMap(report.getAgeDistJson()))
                                                .schoolDist(parseJsonList(report.getSchoolDistJson()))
                                                .build()
                                )
                                .perception(
                                        ReportDetailResponseDto.Perception.builder()
                                                .preferenceScore(report.getPreferenceScore())
                                                .npsScore(report.getNpsScore())
                                                .build()
                                )
                                .purchaseIntent(
                                        ReportDetailResponseDto.PurchaseIntent.builder()
                                                .purchaseIntentPct(report.getPurchaseIntentPct())
                                                .build()
                                )
                                .build()
                )
                .qualitativeInsight(
                        ReportDetailResponseDto.QualitativeInsight.builder()
                                .positiveSummary(report.getPositiveSummary())
                                .negativeSummary(report.getNegativeSummary())
                                .improvementSuggestions(report.getImprovementSuggestions())
                                .media(List.of())
                                .rawFeedbackSamples(List.of())
                                .build()
                )
                .finance(
                        ReportDetailResponseDto.Finance.builder()
                                .unitPrice(report.getUnitPrice())
                                .billingTotal(report.getBillingTotal())
                                .billingFee(report.getBillingFee())
                                .billingRemaining(report.getBillingRemaining())
                                .refundInfo(report.getRefundInfo())
                                .pricingFormula(report.getPricingFormula())
                                .build()
                )
                .visualization(
                        ReportDetailResponseDto.Visualization.builder()
                                .kpiCharts(parseJsonGeneric(report.getKpiChartsJson()))
                                .surveyStats(parseJsonGeneric(report.getSurveyStatsJson()))
                                .heatmap(parseJsonGeneric(report.getHeatmapJson()))
                                .build()
                )
                .extraValue(
                        ReportDetailResponseDto.ExtraValue.builder()
                                .industryBenchmark(report.getIndustryBenchmark())
                                .competitorBenchmark(report.getCompetitorBenchmark())
                                .roiEstimate(report.getRoiEstimate())
                                .build()
                )
                .pdf(
                        ReportDetailResponseDto.PdfInfo.builder()
                                .pdfUrl(report.getReportPdfUrl())
                                .available(report.getReportPdfUrl() != null)
                                .build()
                )
                .build();
    }
}
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
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportQueryService {

    private final SamplingReportRepository samplingReportRepository;
    private final ObjectMapper objectMapper;
    private LocalDate startAt;
    private LocalDate endAt;


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


    // ==============================
    //  리포트 목록 조회 (기업 전용)
    // ==============================
    public List<ReportListResponseDto> getReportList(
            Long userId,
            LocalDate startDate,
            LocalDate endDate,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(page, size);

        LocalDateTime start = (startDate != null)
                ? startDate.atStartOfDay()
                : LocalDateTime.of(1970, 1, 1, 0, 0);

        LocalDateTime end = (endDate != null)
                ? endDate.atTime(23, 59, 59)
                : LocalDateTime.of(2100, 12, 31, 23, 59);

        Page<SamplingReport> reports =
                samplingReportRepository.findReportsByCompanyUserId(
                        userId, start, end, pageable
                );

        return reports.stream()
                .map(r -> {
                    var c = r.getCollaboration()
                            .getMatchRequest()
                            .getCampaign();

                    return ReportListResponseDto.builder()
                            .reportId(r.getReportId())
                            .eventName(c.getName())
                            .productName(c.getProductName())
                            .brandName(c.getBrandName())
                            .createdAt(r.getCreatedAt())
                            .organizationName(c.getStudentOrg().getOrganizationName())
                            .hasPdf(r.getReportPdfUrl() != null)
                            .status(r.getStatus().name())
                            .build();
                })
                .collect(Collectors.toList());
    }


    // ==============================
    //  리포트 상세 조회
    // ==============================
    public ReportDetailResponseDto getReportDetail(Long reportId) {

        SamplingReport report = samplingReportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("리포트를 찾을 수 없습니다."));

        var campaign = report.getCollaboration().getMatchRequest()
                .getCampaign();

        return ReportDetailResponseDto.builder()
                .basicInfo(
                        ReportDetailResponseDto.BasicInfo.builder()
                                .eventTitle(campaign.getName())
                                .brandName(campaign.getBrandName())
                                .productName(campaign.getProductName())
                                .period(
                                        ReportDetailResponseDto.Period.builder()
                                                .startAt(campaign.getStartDate())
                                                .endAt(campaign.getEndDate())
                                                .build()
                                )
                                .location(campaign.getLocationName())
                                .targetDesc(campaign.getTargetDesc())
                                .plannedQuantity(campaign.getProductQuantity())
                                .distributedQuantity(campaign.getDistributedQuantity())
                                .cost(
                                        ReportDetailResponseDto.Cost.builder()
                                                .laborCost(campaign.getLaborCost())
                                                .etcCost(campaign.getEtcCost())
                                                .totalCost(campaign.getTotalCost())
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
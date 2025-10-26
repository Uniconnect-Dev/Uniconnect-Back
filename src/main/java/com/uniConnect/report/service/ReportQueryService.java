package com.uniConnect.report.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 리포트 목록 조회 (/api/reports)
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
     * 리포트 상세 조회 (/api/reports/{reportId})
     */
    public ReportDetailResponseDto getReportDetail(Long reportId) {

        // 접근 권한 체크
        accessValidator.validateCanAccessReport(reportId);

        // 리포트 본문
        SamplingReport report = samplingReportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Report not found: " + reportId));

        // 연결된 캠페인
        var campaign = report.getCampaign();

        // 부가 데이터
        List<SamplingReportMedia> mediaList = mediaRepository.findByReport_ReportId(reportId);
        List<SamplingReportFeedback> feedbackList = feedbackRepository.findByReport_ReportId(reportId);

        // JSON 파싱
        Map<String, Object> genderDist = parseJsonMap(report.getGenderDistJson());
        Map<String, Object> ageDist = parseJsonMap(report.getAgeDistJson());
        List<Map<String, Object>> schoolDist = parseJsonList(report.getSchoolDistJson());

        Object heatmap = parseJsonGeneric(report.getHeatmapDataJson());
        Object surveyStats = parseJsonGeneric(report.getSurveyStatsJson());

        // 미디어 DTO 변환
        List<ReportDetailResponseDto.MediaItem> mediaDtoList = mediaList.stream()
                .map(m -> ReportDetailResponseDto.MediaItem.builder()
                        .type(m.getMediaType().name())
                        .url(m.getMediaUrl())
                        .caption(m.getCaption())
                        .build())
                .toList();

        // 피드백 DTO 변환
        List<ReportDetailResponseDto.FeedbackItem> feedbackDtoList = feedbackList.stream()
                .map(f -> ReportDetailResponseDto.FeedbackItem.builder()
                        .quote(f.getQuote())
                        .isPositive(f.getIsPositive())
                        .build())
                .toList();

        // 기본 정보 섹션
        ReportDetailResponseDto.BasicInfo basicInfo =
                ReportDetailResponseDto.BasicInfo.builder()
                        .eventTitle(campaign.getName())
                        .brandName(campaign.getBrandName())
                        .productName(campaign.getProductName())
                        .period(
                                ReportDetailResponseDto.Period.builder()
                                        .startAt(campaign.getStartDate() != null ? campaign.getStartDate().toString() : null)
                                        .endAt(campaign.getEndDate() != null ? campaign.getEndDate().toString() : null)
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
                        .build();

        // KPI 섹션
        ReportDetailResponseDto.Kpi kpi =
                ReportDetailResponseDto.Kpi.builder()
                        .reach(
                                ReportDetailResponseDto.Reach.builder()
                                        .distributedQuantity(campaign.getDistributedQuantity())
                                        .reachCount(report.getReachCount())
                                        .onSiteExposureCount(report.getExposureCount())
                                        .snsMentions(report.getSnsMentions())
                                        .build()
                        )
                        .engagement(
                                ReportDetailResponseDto.Engagement.builder()
                                        .genderDist(genderDist)
                                        .ageDist(ageDist)
                                        .schoolDist(schoolDist)
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
                        .build();

        // 정성 인사이트 섹션
        ReportDetailResponseDto.QualitativeInsight qual =
                ReportDetailResponseDto.QualitativeInsight.builder()
                        .media(mediaDtoList)
                        .rawFeedbackSamples(feedbackDtoList)
                        .positiveSummary(report.getQualPositiveSummary())
                        .negativeSummary(report.getQualNegativeSummary())
                        .improvementSuggestions(report.getImprovementSuggestions())
                        .build();

        // 재무/정산 섹션
        String pricingFormula = buildPricingFormula(
                campaign.getDistributedQuantity(),
                report.getUnitPrice(),
                campaign.getEtcCost()
        );

        ReportDetailResponseDto.Finance finance =
                ReportDetailResponseDto.Finance.builder()
                        .unitPrice(report.getUnitPrice())
                        .billingTotal(report.getBillingTotal())
                        .billingFee(report.getBillingFee())
                        .billingRemaining(report.getBillingRemaining())
                        .refundInfo(report.getRefundInfo())
                        .pricingFormula(pricingFormula)
                        .build();

        // 시각화 섹션
        ReportDetailResponseDto.Visualization visualization =
                ReportDetailResponseDto.Visualization.builder()
                        .kpiCharts(null)
                        .surveyStats(surveyStats)
                        .heatmap(heatmap)
                        .build();

        // 추가 가치 섹션
        ReportDetailResponseDto.ExtraValue extraValue =
                ReportDetailResponseDto.ExtraValue.builder()
                        .industryBenchmark(report.getBenchmarkText())
                        .competitorBenchmark(report.getBenchmarkText())
                        .roiEstimate(report.getRoiEstimateText())
                        .build();

        // pdf 섹션
        ReportDetailResponseDto.PdfInfo pdfInfo =
                ReportDetailResponseDto.PdfInfo.builder()
                        .pdfUrl(report.getReportPdfUrl())
                        .available(report.getReportPdfUrl() != null)
                        .build();

        // 최종 조립
        return ReportDetailResponseDto.builder()
                .basicInfo(basicInfo)
                .kpi(kpi)
                .qualitativeInsight(qual)
                .finance(finance)
                .visualization(visualization)
                .extraValue(extraValue)
                .pdf(pdfInfo)
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

    private String buildPricingFormula(Integer distributedQty, Integer unitPrice, Integer etcCost) {
        StringBuilder sb = new StringBuilder();

        if (distributedQty != null && unitPrice != null) {
            sb.append(distributedQty)
                    .append("명 × ")
                    .append(unitPrice)
                    .append("원");
        }

        if (etcCost != null && etcCost > 0) {
            if (!sb.isEmpty()) {
                sb.append(" + ");
            }
            sb.append("옵션비 ")
                    .append(etcCost)
                    .append("원");
        }

        return sb.toString();
    }
}


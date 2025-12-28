package com.uniConnect.report.service;

import com.uniConnect.campaign.entity.Campaign;
import com.uniConnect.campaign.enums.CampaignStatus;
import com.uniConnect.campaign.repository.CampaignRepository;
import com.uniConnect.global.exception.CustomException;
import com.uniConnect.global.exception.ErrorCode;
import com.uniConnect.notification.service.AdminNotificationService;
import com.uniConnect.report.dto.request.SamplingReportRequest;
import com.uniConnect.report.entity.SamplingReport;
import com.uniConnect.report.entity.SamplingReportFeedback;
import com.uniConnect.report.entity.SamplingReportMedia;
import com.uniConnect.report.enums.SamplingReportStatus;
import com.uniConnect.report.repository.SamplingReportFeedbackRepository;
import com.uniConnect.report.repository.SamplingReportMediaRepository;
import com.uniConnect.report.repository.SamplingReportRepository;
import com.uniConnect.s3.S3FileService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SamplingReportService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String normalizeJson(String json) {
        if (json == null) return null;

        json = json.trim();
        if (json.isEmpty() || json.equalsIgnoreCase("null")) return null;

        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode node = mapper.readTree(json);

            if (!node.isObject() && !node.isArray()) {
                return null;
            }

            return json;

        } catch (Exception e) {
            return null;
        }
    }

    private String convertJsonNodeToString(JsonNode node) {
        if (node == null) return null;
        try {
            return objectMapper.writeValueAsString(node);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "JSON 변환 실패");
        }
    }


    private final SamplingReportRepository reportRepository;
    private final SamplingReportFeedbackRepository feedbackRepository;
    private final SamplingReportMediaRepository mediaRepository;
    private final CampaignRepository campaignRepository;
    private final S3FileService s3FileService;
    private final AdminNotificationService adminNotificationService;

    @Value("${app.s3.bucket}")
    private String bucketName;

    private String sanitizeJson(String value) {
        if (value == null) return null;
        value = value.trim();
        if (value.isEmpty()) return null;
        if (value.equalsIgnoreCase("null")) return null;
        return value;
    }


    /** 학생단체가 리포트 제출 */
    @Transactional
    public SamplingReport submitReport(
            SamplingReportRequest request,
            List<MultipartFile> mediaFiles,
            Long studentOrgId
    ) {
        Campaign campaign = campaignRepository.findById(request.getCampaignId())
                .orElseThrow(() -> new CustomException(ErrorCode.CAMPAIGN_NOT_FOUND));

        // 캠페인 상태 체크
        if (campaign.getStatus() != CampaignStatus.ReportUploadPending) {
            throw new CustomException(ErrorCode.INVALID_CAMPAIGN_STATE,
                    "캠페인이 리포트 업로드 대기 상태가 아닙니다.");
        }

        // 소유 단체 체크
        if (!campaign.getStudentOrg().getStudentOrgId().equals(studentOrgId)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED,
                    "이 학생단체는 본 캠페인의 리포트를 제출할 권한이 없습니다.");
        }

        // JSON 필드를 JsonNode로 변환 (try/catch 필수)
        JsonNode genderNode = parseJson(request.getGenderDistJson());
        JsonNode ageNode = parseJson(request.getAgeDistJson());
        JsonNode schoolNode = parseJson(request.getSchoolDistJson());
        JsonNode surveyNode = parseJson(request.getSurveyStatsJson());
        JsonNode heatmapNode = parseJson(request.getHeatmapJson());
        JsonNode kpiNode = parseJson(request.getKpiChartsJson());

        SamplingReport report = SamplingReport.builder()
                .campaign(campaign)
                .eventName(request.getEventName())
                .brandName(request.getBrandName())
                .productName(request.getProductName())
                .period(request.getPeriod())
                .location(request.getLocation())
                .target(request.getTarget())
                .totalQuantity(request.getTotalQuantity())
                .distributedQuantity(request.getDistributedQuantity())
                .reachCount(request.getReachCount())
                .exposureCount(request.getExposureCount())
                .snsMentions(request.getSnsMentions())

                .genderDistJson(convertJsonNodeToString(genderNode))
                .ageDistJson(convertJsonNodeToString(ageNode))
                .schoolDistJson(convertJsonNodeToString(schoolNode))
                .surveyStatsJson(convertJsonNodeToString(surveyNode))
                .heatmapJson(convertJsonNodeToString(heatmapNode))
                .kpiChartsJson(convertJsonNodeToString(kpiNode))

                .status(SamplingReportStatus.PendingApproval)
                .build();

        SamplingReport saved = reportRepository.save(report);

        // 피드백
        feedbackRepository.save(
                SamplingReportFeedback.builder()
                        .report(saved)
                        .quote(request.getFeedbackText())
                        .isPositive(null)
                        .build()
        );

        // 미디어 저장
        if (mediaFiles != null && !mediaFiles.isEmpty()) {
            for (MultipartFile file : mediaFiles) {
                try {
                    String key = "reports/" + UUID.randomUUID() + "_" + file.getOriginalFilename();
                    s3FileService.upload(bucketName, key, file);

                    mediaRepository.save(
                            SamplingReportMedia.builder()
                                    .report(saved)
                                    .mediaUrl("https://" + bucketName + ".s3.amazonaws.com/" + key)
                                    .mediaType(file.getContentType().startsWith("video")
                                            ? SamplingReportMedia.MediaType.VIDEO
                                            : SamplingReportMedia.MediaType.IMAGE)
                                    .build()
                    );
                } catch (Exception e) {
                    throw new RuntimeException("S3 업로드 실패: " + e.getMessage());
                }
            }
        }

        adminNotificationService.notifyAdmin("리포트 제출됨: " + request.getEventName());
        return saved;
    }

    private JsonNode parseJson(String raw) {
        if (raw == null || raw.isBlank() || raw.equalsIgnoreCase("null")) {
            return null;
        }
        try {
            return objectMapper.readTree(raw);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE,
                    "잘못된 JSON 형식입니다: " + raw);
        }
    }

    @Transactional
    public void approveReport(Long reportId) {
        SamplingReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new CustomException(ErrorCode.REPORT_NOT_FOUND));

        report.setStatus(SamplingReportStatus.Approved);
        reportRepository.save(report);

        Campaign campaign = report.getCampaign();
        campaign.setStatus(CampaignStatus.Completed);
        campaignRepository.save(campaign);
    }

    @Transactional
    public void updateReportPdfUrl(Long reportId, String pdfUrl) {
        SamplingReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new CustomException(ErrorCode.REPORT_NOT_FOUND));

        report.setReportPdfUrl(pdfUrl);
        reportRepository.save(report);
    }

    @Transactional
    public void rejectReport(Long reportId) {
        SamplingReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new CustomException(ErrorCode.REPORT_NOT_FOUND));

        report.setStatus(SamplingReportStatus.Rejected);
        reportRepository.save(report);
    }

}
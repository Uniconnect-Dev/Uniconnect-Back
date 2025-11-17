package com.uniConnect.report.service;

import com.uniConnect.campaign.entity.Campaign;
import com.uniConnect.campaign.repository.CampaignRepository;
import com.uniConnect.global.exception.CustomException;
import com.uniConnect.global.exception.ErrorCode;
import com.uniConnect.notification.service.*;
import com.uniConnect.s3.*;
import com.uniConnect.report.dto.request.SamplingReportRequest;
import com.uniConnect.report.entity.*;
import com.uniConnect.report.enums.SamplingReportStatus;
import com.uniConnect.campaign.enums.CampaignStatus;
import com.uniConnect.report.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SamplingReportService {

    private final SamplingReportRepository reportRepository;
    private final SamplingReportFeedbackRepository feedbackRepository;
    private final SamplingReportMediaRepository mediaRepository;
    private final CampaignRepository campaignRepository;
    private final S3FileService s3FileService;
    private final AdminNotificationService adminNotificationService;

    @Value("${app.s3.bucket}")
    private String bucketName;

    /** 학생단체가 리포트 제출 */
    @Transactional
    public SamplingReport submitReport(SamplingReportRequest request,
                                       List<MultipartFile> mediaFiles,
                                       Long studentOrgId) {

        Campaign campaign = campaignRepository.findById(request.getCampaignId())
                .orElseThrow(() -> new CustomException(ErrorCode.CAMPAIGN_NOT_FOUND));

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
                .status(SamplingReportStatus.PendingApproval)
                .build();

        SamplingReport saved = reportRepository.save(report);

        SamplingReportFeedback feedback = SamplingReportFeedback.builder()
                .report(saved)
                .quote(request.getFeedbackText())
                .isPositive(null)
                .build();
        feedbackRepository.save(feedback);

        if (mediaFiles != null && !mediaFiles.isEmpty()) {
            for (MultipartFile file : mediaFiles) {
                try {
                    String key = "reports/" + UUID.randomUUID() + "_" + file.getOriginalFilename();
                    s3FileService.upload(bucketName, key, file);
                    String url = "https://" + bucketName + ".s3.amazonaws.com/" + key;

                    SamplingReportMedia media = SamplingReportMedia.builder()
                            .report(saved)
                            .mediaUrl(url)
                            .mediaType(file.getContentType().startsWith("video")
                                    ? SamplingReportMedia.MediaType.VIDEO
                                    : SamplingReportMedia.MediaType.IMAGE)
                            .build();
                    mediaRepository.save(media);

                } catch (Exception e) {
                    throw new RuntimeException("S3 업로드 실패: " + e.getMessage());
                }
            }
        }

        adminNotificationService.notifyAdmin("리포트 제출: " + request.getEventName());

        return saved;
    }

    /** 어드민 승인 */
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

}
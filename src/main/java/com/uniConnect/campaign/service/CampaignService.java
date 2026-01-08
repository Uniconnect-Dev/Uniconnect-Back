package com.uniConnect.campaign.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uniConnect.member.entity.User;
import com.uniConnect.member.repository.UserRepository;
import com.uniConnect.campaign.dto.CampaignCreateRequest;
import com.uniConnect.campaign.dto.CampaignFirstPageResponse;
import com.uniConnect.campaign.dto.CampaignFirstPageSaveRequest;
import com.uniConnect.campaign.entity.Campaign;
import com.uniConnect.campaign.entity.CampaignTarget;
import com.uniConnect.campaign.enums.CampaignStatus;
import com.uniConnect.campaign.repository.CampaignRepository;
import com.uniConnect.campaign.repository.CampaignTargetRepository;
import com.uniConnect.studentOrg.entity.Hashtag;
import com.uniConnect.studentOrg.entity.StudentOrg;
import com.uniConnect.studentOrg.enums.HashtagCategory;
import com.uniConnect.studentOrg.repository.HashtagRepository;
import com.uniConnect.studentOrg.repository.StudentOrgRepository;
import com.uniConnect.s3.S3FileService;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CampaignService {

    private final CampaignRepository campaignRepository;
    private final CampaignTargetRepository campaignTargetRepository;
    private final HashtagRepository hashtagRepository;
    private final ObjectMapper objectMapper;
    private final StudentOrgRepository studentOrgRepository;
    private final S3FileService s3FileService;
    private final UserRepository userRepository;
    @Value("${app.s3.bucket}")
    private String bucketName;

    /**
     * 캠페인 첫 페이지 조회
     */
    @Transactional(readOnly = true)
    public CampaignFirstPageResponse getFirstPage(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자가 존재하지 않습니다."));

        StudentOrg studentOrg = user.getStudentOrg();
        if (studentOrg == null) {
            throw new IllegalStateException("학생단체 소속 사용자만 캠페인을 생성할 수 있습니다.");
        }

        return CampaignFirstPageResponse.builder()
                .schoolName(studentOrg.getSchoolName())
                .organizationName(studentOrg.getOrganizationName())
                .build();
    }

    /**
     * 캠페인 첫 페이지 저장 (캠페인 생성)
     */
    public Long saveFirstPage(
            Long userId,
            CampaignFirstPageSaveRequest request
    ) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자가 존재하지 않습니다."));

        StudentOrg studentOrg = user.getStudentOrg();
        if (studentOrg == null) {
            throw new IllegalStateException("학생단체 소속 사용자만 캠페인을 생성할 수 있습니다.");
        }

        Campaign campaign = Campaign.builder()
                .studentOrg(studentOrg)
                .managerName(request.getManagerName())
                .managerPhone(request.getManagerPhone())
                .managerEmail(request.getManagerEmail())
                .status(CampaignStatus.Draft)
                .build();

        campaignRepository.save(campaign);
        return campaign.getCampaignId();
    }

    /**
     * 한 페이지 입력 → Campaign 생성
     */
    public Long createCampaign(Long userId, CampaignCreateRequest dto) {

        StudentOrg studentOrg = studentOrgRepository.findByUsers_UserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("StudentOrg not found"));

        Campaign campaign = campaignRepository.save(
                Campaign.builder()
                        .name(dto.name())
                        .startDate(dto.startDate())
                        .endDate(dto.endDate())
                        .locationName(dto.locationName())
                        .purpose(dto.purpose())
                        .collaborationType(dto.collaborationType())

                        .productName(dto.productName())
                        .productQuantity(dto.productQuantity())

                        .expectedParticipants(dto.expectedParticipants())
                        .expectedExposures(dto.expectedExposures())
                        .targetAgeDesc(dto.targetAgeDesc())
                        .targetMajorDesc(dto.targetMajorDesc())

                        .preferredIndustry1(dto.preferredIndustry1())
                        .preferredIndustry2(dto.preferredIndustry2())
                        .recommendedSamplingQty(dto.recommendedSamplingQty())
                        .boothFee(dto.boothFee())

                        .extraRequest(dto.extraRequest())
                        .proposalFileUrl(dto.proposalFileUrl())

                        .status(CampaignStatus.Draft)
                        .studentOrg(studentOrg)
                        .build()
        );

        saveTargets(campaign, HashtagCategory.StudentType, dto.studentTypeTagIds());
        saveTargets(campaign, HashtagCategory.Region, dto.regionTagIds());
        saveTargets(campaign, HashtagCategory.Hobby, dto.hobbyTagIds());
        saveTargets(campaign, HashtagCategory.Lifestyle, dto.lifestyleTagIds());

        try {
            if (dto.eventPrograms() != null) {
                campaign.setEventPrograms(
                        objectMapper.valueToTree(dto.eventPrograms())
                );
            }

            if (dto.marketingMethods() != null) {
                campaign.setMarketingMethods(
                        objectMapper.valueToTree(dto.marketingMethods())
                );
            }

            if (dto.promotionPlans() != null) {
                campaign.setPromotionPlans(
                        objectMapper.valueToTree(dto.promotionPlans())
                );
            }
        } catch (Exception e) {
            throw new IllegalStateException("캠페인 JSON 데이터 저장 실패", e);
        }

        return campaign.getCampaignId();
    }

    public void submit(Long campaignId, Long userId) {
        Campaign campaign = findById(campaignId);
        if (!campaign.getStudentOrg().hasUser(userId)) {
            throw new IllegalStateException("권한이 없습니다.");
        }
        if (campaign.getStatus() != CampaignStatus.Draft) {
            throw new IllegalStateException("이미 제출된 캠페인입니다.");
        }

        campaign.setStatus(CampaignStatus.Submitted);
    }

    private Campaign findById(Long campaignId) {
        return campaignRepository.findById(campaignId)
                .orElseThrow(() -> new IllegalArgumentException("Campaign not found"));
    }

    private void saveTargets(
            Campaign campaign,
            HashtagCategory category,
            List<Long> tagIds
    ) {
        if (tagIds == null || tagIds.isEmpty()) return;

        List<Hashtag> hashtags = hashtagRepository.findAllById(tagIds);

        for (Hashtag hashtag : hashtags) {
            CampaignTarget target = CampaignTarget.builder()
                    .campaign(campaign)
                    .hashtag(hashtag)
                    .hashtagCategory(category)
                    .build();

            campaignTargetRepository.save(target);
        }
    }

    @Transactional
    public String uploadProposalFile(
            Long campaignId,
            Long userId,
            MultipartFile file
    ) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new IllegalArgumentException("Campaign not found"));

        if (!campaign.getStudentOrg().getUsers().stream()
                .anyMatch(u -> u.getUserId().equals(userId))) {
            throw new IllegalStateException("제안서 업로드 권한이 없습니다.");
        }

        String key = "campaign-proposals/"
                + campaignId + "_"
                + System.currentTimeMillis() + "_"
                + file.getOriginalFilename();

        try {
            s3FileService.upload(bucketName, key, file);
        } catch (Exception e) {
            throw new RuntimeException("S3 제안서 업로드 실패", e);
        }

        String url = "https://" + bucketName + ".s3.ap-northeast-2.amazonaws.com/" + key;

        campaign.setProposalFileUrl(url);
        campaign.setStatus(CampaignStatus.Submitted);

        return url;
    }
}

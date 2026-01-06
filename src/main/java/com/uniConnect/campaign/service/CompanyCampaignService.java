package com.uniConnect.campaign.service;

import com.uniConnect.campaign.dto.CompanyCampaignDetailResponse;
import com.uniConnect.campaign.dto.CompanyCampaignListResponse;
import com.uniConnect.campaign.entity.Campaign;
import com.uniConnect.campaign.repository.CampaignRepository;
import com.uniConnect.studentOrg.entity.Hashtag;
import com.uniConnect.studentOrg.enums.CollaborationType;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompanyCampaignService {

    private final CampaignRepository campaignRepository;

    public Page<CompanyCampaignListResponse> getAllCampaigns(Pageable pageable) {
        return campaignRepository.findCampaigns(pageable);
    }

    public Page<CompanyCampaignListResponse> searchCampaigns(
            String keyword,
            CollaborationType collaborationType,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable
    ) {
        return campaignRepository.searchCampaigns(
                keyword,
                collaborationType,
                startDate,
                endDate,
                pageable
        );
    }

    public CompanyCampaignDetailResponse getCampaignDetail(Long campaignId) {
        Campaign campaign = campaignRepository.findDetailById(campaignId)
                .orElseThrow(() -> new IllegalArgumentException("캠페인이 존재하지 않습니다."));

        return CompanyCampaignDetailResponse.builder()
                .name(campaign.getName())
                .locationName(campaign.getLocationName())
                .startDate(campaign.getStartDate())
                .endDate(campaign.getEndDate())
                .purpose(campaign.getPurpose())
                .expectedParticipants(campaign.getExpectedParticipants())
                .expectedExposures(campaign.getExpectedExposures())
                .targetAgeDesc(campaign.getTargetAgeDesc())
                .targetMajorDesc(campaign.getTargetMajorDesc())
                .keywords(
                        campaign.getTargetKeywords().stream()
                                .map(Hashtag::getName)
                                .toList()
                )
                .eventPrograms(campaign.getEventPrograms())
                .preferredIndustry1(campaign.getPreferredIndustry1())
                .preferredIndustry2(campaign.getPreferredIndustry2())
                .recommendedSamplingQty(campaign.getRecommendedSamplingQty())
                .boothFee(campaign.getBoothFee())
                .promotionPlans(campaign.getPromotionPlans())
                .marketingMethods(campaign.getMarketingMethods())
                .extraRequest(campaign.getExtraRequest())
                .schoolName(campaign.getStudentOrg().getSchoolName())
                .organizationName(campaign.getStudentOrg().getOrganizationName())
                .logoUrl(campaign.getStudentOrg().getLogoUrl())
                .build();
    }
}

package com.uniConnect.campaign.dto;

import com.uniConnect.studentOrg.enums.CollaborationType;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record CampaignCreateRequest(

        // 기본 정보
        String name,
        LocalDate startDate,
        LocalDate endDate,
        String locationName,
        String purpose,
        CollaborationType collaborationType,

        // 샘플링 정보
        String productName,
        Integer productQuantity,

        Integer expectedParticipants,
        Integer expectedExposures,

        String targetAgeDesc,
        String targetMajorDesc,

        String preferredIndustry1,
        String preferredIndustry2,

        Integer recommendedSamplingQty,
        Integer boothFee,

        String extraRequest,
        String proposalFileUrl,

        // 타깃 해시태그 (4종)
        List<Long> studentTypeTagIds,
        List<Long> regionTagIds,
        List<Long> hobbyTagIds,
        List<Long> lifestyleTagIds,

        // JSON
        Object eventPrograms,
        Object marketingMethods,
        Object promotionPlans

) {}

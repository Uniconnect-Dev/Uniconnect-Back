package com.uniConnect.campaign.controller;

import com.uniConnect.campaign.dto.CompanyCampaignDetailResponse;
import com.uniConnect.campaign.dto.CompanyCampaignListResponse;
import com.uniConnect.campaign.service.CompanyCampaignService;
import com.uniConnect.studentOrg.enums.CollaborationType;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort;
import org.springdoc.core.annotations.ParameterObject;

import java.time.LocalDate;

@Tag(name = "Company Campaign Search", description = "기업용 학생단체 캠페인 탐색 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/company/campaigns")
public class CompanyCampaignController {

    private final CompanyCampaignService campaignService;

    @Operation(
            summary = "학생단체 캠페인 전체 조회",
            description = "기업이 학생단체가 등록한 모든 캠페인을 최신순으로 탐색합니다."
    )
    @GetMapping
    public Page<CompanyCampaignListResponse> getCampaigns(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            @ParameterObject Pageable pageable
    ) {
        return campaignService.getAllCampaigns(pageable);
    }

    @Operation(
            summary = "학생단체 캠페인 필터 검색",
            description = "행사명/학교명, 기간, 협업 유형 조건으로 학생단체 캠페인을 검색합니다."
    )
    @GetMapping("/search")
    public Page<CompanyCampaignListResponse> searchCampaigns(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) CollaborationType collaborationType,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            @ParameterObject Pageable pageable

    ) {
        return campaignService.searchCampaigns(
                keyword,
                collaborationType,
                startDate,
                endDate,
                pageable
        );
    }

    @Operation(
            summary = "캠페인 상세 조회",
            description = "선택한 학생단체 캠페인의 상세 정보를 조회합니다."
    )
    @GetMapping("/{campaignId}")
    public CompanyCampaignDetailResponse getCampaignDetail(
            @PathVariable Long campaignId
    ) {
        return campaignService.getCampaignDetail(campaignId);
    }
}


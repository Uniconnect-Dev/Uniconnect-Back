package com.uniConnect.recommendation.controller;

import com.uniConnect.recommendation.service.*;
import com.uniConnect.recommendation.dto.*;
import com.uniConnect.global.response.ApiResponse;
import com.uniConnect.member.security.local.CustomUser;
import com.uniConnect.recommendation.dto.RecommendedSamplingCompanyResponse;
import com.uniConnect.recommendation.service.SamplingCompanyRecommendationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sampling/campaigns")
@Tag(name = "Sampling Company Recommendation", description = "학생단체 협업 시 기업 추천 API")
public class SamplingCompanyRecommendationController {

    private final SamplingCompanyRecommendationService recommendationService;

    @GetMapping("/recommended-companies")
    @Operation(summary = "로그인한 학생단체의 제일 최근 협업 기준 추천 기업 조회")
    public ApiResponse<List<RecommendedSamplingCompanyResponse>> recommend(
            @AuthenticationPrincipal CustomUser user
    ) {
        if (user == null || user.getUserId() == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }

        return ApiResponse.success(
                recommendationService.getRecommendedCompaniesByLoginUser(
                        user.getUserId()
                )
        );
    }
}
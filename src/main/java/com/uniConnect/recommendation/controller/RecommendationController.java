package com.uniConnect.recommendation.controller;

import com.uniConnect.global.response.ApiResponse;
import com.uniConnect.member.security.local.CustomUser;
import com.uniConnect.recommendation.dto.RecommendedCompanyResponse;
import com.uniConnect.recommendation.service.RecommendationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Content;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recommendations")
@Tag(name = "Recommendation", description = "학생단체 대상 기업 추천 API")
public class RecommendationController {

    private final RecommendationService recommendationService;

    @Operation(
            summary = "추천 기업 리스트 조회",
            description = """
                    JWT 기반으로 학생단체 ID를 자동 조회하여  
                    매칭된 해시태그 기준으로 기업 추천 리스트를 생성합니다.
                    매칭 태그는 최대 2개만 포함됩니다.
                    """
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "추천 리스트 조회 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "JWT 인증 실패"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "학생단체 정보를 찾을 수 없음"
            )
    })
    @GetMapping("/companies")
    public ApiResponse<List<RecommendedCompanyResponse>> getRecommendedCompanies(
            Authentication authentication,

            @Parameter(description = "이미 선택한 기업 수 (0~5)", example = "0")
            @RequestParam(defaultValue = "0") int selectedCount
    ) {

        CustomUser user = (CustomUser) authentication.getPrincipal();
        Long userId = user.getUserId();

        return ApiResponse.success(
                recommendationService.getRecommendedCompanies(userId, selectedCount)
        );
    }


    @Operation(
            summary = "예상 비용 계산",
            description = "선택된 기업 개수 × 50,000원으로 예상 비용을 계산합니다."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "비용 계산 성공"
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "선택된 기업 ID 리스트",
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(example = """
                        {
                          "selectedCompanyIds": [3, 5, 10]
                        }
                        """)
            )
    )
    @PostMapping("/calc-cost")
    public ApiResponse<Integer> calcCost(
            @org.springframework.web.bind.annotation.RequestBody Map<String, List<Long>> body
    ) {
        List<Long> ids = body.getOrDefault("selectedCompanyIds", List.of());
        return ApiResponse.success(ids.size() * 50000);
    }
}

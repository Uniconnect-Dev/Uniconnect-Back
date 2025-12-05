package com.uniConnect.company.controller;

import com.uniConnect.company.dto.response.CompanyCardResponse;
import com.uniConnect.company.dto.response.CompanyDetailResponse;
import com.uniConnect.company.service.CompanyQueryService;
import com.uniConnect.global.response.ApiResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.uniConnect.member.security.local.CustomUser;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Tag(name = "Company Profile API", description = "기업 프로필 정보 조회 API (학생단체)")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/companies")
public class CompanyController {

    private final CompanyQueryService companyQueryService;
    private Long studentOrgId;

    @Operation(summary = "기업 리스트 조회")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "기업 리스트 조회 성공",
                    content = @Content(
                            array = @ArraySchema(schema = @Schema(implementation = CompanyCardResponse.class))
                    )
            )
    })
    @GetMapping("/list")
    public ApiResponse<List<CompanyCardResponse>> getCompanyList(
            @AuthenticationPrincipal CustomUser user
    ) {
        return ApiResponse.success(companyQueryService.getCompanyList());
    }

    @Operation(summary = "기업 상세 조회")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "기업 상세 정보 조회 성공",
                    content = @Content(schema = @Schema(implementation = CompanyDetailResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "기업을 찾을 수 없음"
            )
    })
    @GetMapping("/{companyId}")
    public ApiResponse<CompanyDetailResponse> getCompanyDetail(
            @Parameter(description = "조회할 기업 ID", example = "1")
            @PathVariable Long companyId
    ) {
        return ApiResponse.success(companyQueryService.getCompanyDetail(companyId));
    }
}

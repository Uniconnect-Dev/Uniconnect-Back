package com.uniConnect.company.controller;

import com.uniConnect.company.service.CompanyInfoService;
import com.uniConnect.global.response.ApiResponse;
import com.uniConnect.member.security.local.CustomUser;
import com.uniConnect.sampling.dto.request.CompanySamplingInfoRequest;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/company")
@RequiredArgsConstructor
public class CompanyInfoController {

    private final CompanyInfoService companyInfoService;

    @PutMapping("/info")
    public ApiResponse<String> saveCompanyInfo(
            @AuthenticationPrincipal CustomUser user,
            @Valid @RequestBody CompanySamplingInfoRequest dto
    ) {
        companyInfoService.updateCompanyInfo(user.getUserId(), dto);
        return ApiResponse.success("기업 샘플링 정보 저장 완료");
    }
}
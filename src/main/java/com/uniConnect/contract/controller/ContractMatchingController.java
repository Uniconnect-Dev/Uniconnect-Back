package com.uniConnect.contract.controller;

import com.uniConnect.contract.dto.MyMatchingListItemDto;
import com.uniConnect.contract.service.ContractMatchingQueryService;
import com.uniConnect.contract.dto.MatchingFilterRequest;
import com.uniConnect.global.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/contracts/matchings")
@Tag(name = "Contract Matching API", description = "계약서 페이지")
public class ContractMatchingController {

    private final ContractMatchingQueryService service;

    @GetMapping("/company")
    @Operation(summary = "기업용 매칭 목록 조회")
    public ApiResponse<List<MyMatchingListItemDto>> getCompanyMatchings() {
        return ApiResponse.success(
                "기업 매칭 목록 조회 성공",
                service.getMyMatchingsForCompany()
        );
    }

    @PostMapping("/company/filter")
    @Operation(summary = "기업용 매칭 목록 필터 조회")
    public ApiResponse<List<MyMatchingListItemDto>> filterCompanyMatchings(
            @RequestBody MatchingFilterRequest filter
    ) {
        return ApiResponse.success(
                "기업 매칭 필터 조회 성공",
                service.getCompanyMatchingsWithFilter(filter)
        );
    }

    @GetMapping("/student-org")
    @Operation(summary = "학생단체용 매칭 목록 조회")
    public ApiResponse<List<MyMatchingListItemDto>> getStudentOrgMatchings() {
        return ApiResponse.success(
                "학생단체 매칭 목록 조회 성공",
                service.getMyMatchingList()
        );
    }

    @PostMapping("/student-org/filter")
    @Operation(summary = "학생단체용 매칭 목록 필터 조회")
    public ApiResponse<List<MyMatchingListItemDto>> filterStudentOrgMatchings(
            @RequestBody MatchingFilterRequest filter
    ) {
        return ApiResponse.success(
                "학생단체 매칭 필터 조회 성공",
                service.getStudentOrgMatchingsWithFilter(filter)
        );
    }
}

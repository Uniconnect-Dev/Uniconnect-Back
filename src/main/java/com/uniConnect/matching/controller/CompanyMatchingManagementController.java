
package com.uniConnect.matching.controller;

import com.uniConnect.company.entity.Company;
import com.uniConnect.matching.dto.*;
import com.uniConnect.matching.service.CompanyMatchingManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/company/matching/management")
@RequiredArgsConstructor
public class CompanyMatchingManagementController {

    private final CompanyMatchingManagementService matchingManagementService;

    /**
     * 기업 매칭 관리 페이지 조회
     */
    @GetMapping
    public ResponseEntity<CompanyMatchingStatisticsDto.CompanyMatchingPageResponse> getMatchingManagementPage(
            @ModelAttribute CompanyMatchingStatisticsDto.CompanyMatchingFilterRequest filter,
            @PageableDefault(size = 10, sort = "requestedAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        CompanyMatchingStatisticsDto.CompanyMatchingPageResponse response = matchingManagementService.getMatchingManagementPage(filter, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * 매칭 통계 조회
     */
    @GetMapping("/statistics")
    public ResponseEntity<CompanyMatchingStatisticsDto.CompanyMatchingStatisticsResponse> getMatchingStatistics() {
        Company company = getCompanyFromLogin();
        CompanyMatchingStatisticsDto.CompanyMatchingStatisticsResponse response =
                matchingManagementService.getMatchingStatistics(company.getCompanyId());
        return ResponseEntity.ok(response);
    }

    /**
     * 매칭 승인
     */
    @PostMapping("/{matchRequestId}/approve")
    public ResponseEntity<Void> approveMatching(@PathVariable Long matchRequestId) {
        matchingManagementService.approveMatching(matchRequestId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * 매칭 거절
     */
    @PostMapping("/{matchRequestId}/reject")
    public ResponseEntity<Void> rejectMatching(@PathVariable Long matchRequestId) {
        matchingManagementService.rejectMatching(matchRequestId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    private Company getCompanyFromLogin() {
        // 로그인 정보에서 기업 조회 로직
        // 실제 구현은 SecurityContextHolder 사용
        return null;
    }
}
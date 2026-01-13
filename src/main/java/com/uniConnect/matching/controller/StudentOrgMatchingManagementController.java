package com.uniConnect.matching.controller;

import com.uniConnect.matching.dto.*;
import com.uniConnect.matching.service.StudentOrgMatchingManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/student-org/matching/management")
@RequiredArgsConstructor
public class StudentOrgMatchingManagementController {

    private final StudentOrgMatchingManagementService matchingManagementService;

    /**
     * 학생단체 매칭 관리 페이지 조회
     */
    @GetMapping
    public ResponseEntity<StudentOrgMatchingStatisticsDto.StudentOrgMatchingPageResponse> getMatchingManagementPage(
            @ModelAttribute StudentOrgMatchingStatisticsDto.StudentOrgMatchingFilterRequest filter,
            @PageableDefault(size = 10, sort = "requestedAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        StudentOrgMatchingStatisticsDto.StudentOrgMatchingPageResponse response =
                matchingManagementService.getMatchingManagementPage(filter, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * 매칭 통계 조회
     */
    @GetMapping("/statistics")
    public ResponseEntity<StudentOrgMatchingStatisticsDto.StudentOrgMatchingStatisticsResponse> getMatchingStatistics() {
        // 로그인한 학생단체 ID 조회 필요
        // 실제 구현시 SecurityContextHolder 사용
        StudentOrgMatchingStatisticsDto.StudentOrgMatchingStatisticsResponse response =
                matchingManagementService.getMatchingStatistics(1L); // 임시값
        return ResponseEntity.ok(response);
    }

    /**
     * 매칭 승인 (받은 요청에 대한 승인)
     */
    @PostMapping("/{matchRequestId}/approve")
    public ResponseEntity<Void> approveMatching(@PathVariable Long matchRequestId) {
        matchingManagementService.approveMatching(matchRequestId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * 매칭 거절 (받은 요청에 대한 거절)
     */
    @PostMapping("/{matchRequestId}/reject")
    public ResponseEntity<Void> rejectMatching(@PathVariable Long matchRequestId) {
        matchingManagementService.rejectMatching(matchRequestId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}

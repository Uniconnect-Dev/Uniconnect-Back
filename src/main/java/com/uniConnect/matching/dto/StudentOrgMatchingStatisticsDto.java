package com.uniConnect.matching.dto;

import com.uniConnect.campaign.enums.MatchingStatus;
import com.uniConnect.matching.enums.MatchSender;
import com.uniConnect.studentOrg.enums.CollaborationType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class StudentOrgMatchingStatisticsDto {
    @Data
    @Builder
    public static class StudentOrgMatchingManagementDto {
        private Long matchRequestId;
        private String companyName;
        private LocalDate campaignStartDate;
        private LocalDate campaignEndDate;
        private LocalDateTime requestedAt;
        private CollaborationType collaborationType;
        private MatchingStatus status;
        private MatchSender sender;
        private String processStatus; // 계약 상태: "계약 전", "계약서 작성 중", "설문지 관리", "제품 발송", "데이터 리포트", "정산/결제", "협업 종료"
        private String contractStatus; // "서명 전", "계약 완료"
        private Long collaborationId;
        private String campaignName;
    }

    @Data @Builder
    public static class StudentOrgMatchingFilterRequest {
        private String companyName;              // 기업명 검색
        private LocalDate campaignStartDateFrom; // 캠페인 시작일 시작
        private LocalDate campaignStartDateTo;   // 캠페인 시작일 종료
        private LocalDate matchingRequestDateFrom; // 매칭 요청일 시작
        private LocalDate matchingRequestDateTo;   // 매칭 요청일 종료
        private CollaborationType collaborationType; // 협업 형태
        private String matchingStatus;             // 매칭 상태: ALL, REQUESTED, APPROVED, REJECTED
        private String processStatus;              // 프로세스: 계약 전, 계약서 작성 중 등

    }

    @Data @Builder
    public static class StudentOrgMatchingStatisticsResponse {
        private Integer totalCount;        // 전체
        private Integer completedCount;    // 매칭 완료
        private Integer pendingCount;      // 대기 중
        private Integer failedCount;       // 매칭 실패
    }

    @Data @Builder
    public static class StudentOrgMatchingPageResponse {
        private StudentOrgMatchingStatisticsResponse statistics;
        private List<StudentOrgMatchingManagementDto> sentMatchings;      // 요청한 매칭 (학생->기업 campaign 제안)
        private List<StudentOrgMatchingManagementDto> receivedMatchings;  // 받은 요청 (기업 proposal)
        private Integer totalPages;
        private Integer currentPage;
        private Integer pageSize;
        private Long totalElements;
    }

}

package com.uniConnect.matching.dto;

import com.uniConnect.studentOrg.enums.CollaborationType;
import com.uniConnect.campaign.enums.MatchingStatus;
import com.uniConnect.matching.enums.MatchSender;
import lombok.*;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;

//static inner class: 맨 위엔 annot x
public class CompanyMatchingStatisticsDto {

    @Data
    @Builder
    public static class CompanyMatchingManagementDto {
        private Long matchRequestId;
        private String studentOrgName;
        private LocalDate desiredCollaborationDate;
        private LocalDateTime requestedAt;
        private CollaborationType collaborationType;
        private MatchingStatus status;
        private MatchSender sender;
        private String processStatus; // 계약 상태: "계약 전", "계약서 작성 중", "설문지 관리", "제품 발송", "데이터 리포트", "정산/결제", "협업 종료"
        private String contractStatus; // "서명 전", "계약 완료"
        private Long collaborationId;
    }

    @Data
    @Builder
    public static class CompanyMatchingFilterRequest {
        private String studentOrgName;              // 학생 단체명 검색
        private LocalDate desiredCollaborationDateFrom; // 희망 협업일 시작
        private LocalDate desiredCollaborationDateTo;   // 희망 협업일 종료
        private LocalDate matchingRequestDateFrom;      // 매칭 요청일 시작
        private LocalDate matchingRequestDateTo;        // 매칭 요청일 종료
        private CollaborationType collaborationType;    // 협업 형태
        private String matchingStatus;                  // 매칭 상태: ALL, REQUESTED, APPROVED, REJECTED
        private String processStatus;
    }


    @Data
    @Builder
    public static class CompanyMatchingStatisticsResponse {
        private Integer totalCount;        // 전체
        private Integer completedCount;    // 매칭 완료
        private Integer pendingCount;      // 대기 중
        private Integer failedCount;       // 매칭 실패
    }

    @Data
    @Builder
    public static class CompanyMatchingPageResponse {
        private CompanyMatchingStatisticsResponse statistics;
        private List<CompanyMatchingManagementDto> sentMatchings;      // 요청한 매칭
        private List<CompanyMatchingManagementDto> receivedMatchings;  // 받은 요청
        private Integer totalPages;
        private Integer currentPage;
        private Integer pageSize;
        private Long totalElements;
    }


}
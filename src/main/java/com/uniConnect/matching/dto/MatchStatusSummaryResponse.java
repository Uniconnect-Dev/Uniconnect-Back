package com.uniConnect.matching.dto;

import lombok.*;

@Getter @Setter @Builder
public class MatchStatusSummaryResponse  {
    private Long sentCount;        // 내가 보낸 매칭 수
    private Long receivedCount;    // 내가 받은 매칭 수
    private Long totalCount;       // 전체 매칭 수

    private Long approvedCount;    // 완료됨
    private Long pendingCount;     // 대기 중
    private Long rejectedCount;    // 실패
}
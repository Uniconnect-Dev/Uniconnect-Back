package com.uniConnect.matching.dto;

import com.uniConnect.campaign.enums.MatchingStatus;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter @Setter @Builder
public class MatchSentItemResponse {

    private Long matchId;
    private Long targetId;       // 기업 ID 또는 학생단체 ID

    private String targetName;
    private String eventTitle;
    private String collaborationType;
    private LocalDate desiredDate;

    private LocalDateTime requestedAt;
    private LocalDateTime respondedAt;

    private MatchingStatus status;
}


package com.uniConnect.matching.dto;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter @Setter @Builder
public class MatchReceivedItemResponse {

    private Long matchId;
    private Long senderId;

    private String senderName;
    private String campaignTitle;
    private String collaborationType;
    private LocalDateTime requestedAt;
}
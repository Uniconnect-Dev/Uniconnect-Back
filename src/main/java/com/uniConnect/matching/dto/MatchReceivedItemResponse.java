package com.uniConnect.matching.dto;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter @Setter @Builder
public class MatchReceivedItemResponse {

    private Long matchId;
    private Long senderId;

    private String senderName;
    private String eventTitle;
    private String collaborationType;
    private LocalDate desiredDate;
    private LocalDateTime requestedAt;
}
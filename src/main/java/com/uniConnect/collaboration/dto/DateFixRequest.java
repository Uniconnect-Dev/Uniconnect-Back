package com.uniConnect.collaboration.dto;

import lombok.*;
import java.time.LocalDate;

@Data
@Builder
public class DateFixRequest {
    private Long collaborationId;
    private LocalDate eventDate; // 행사 날짜(=deadline 기준으로 task에 넣어줄 값)
}

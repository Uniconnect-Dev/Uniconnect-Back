package com.uniConnect.collaboration.dto;

import com.uniConnect.collaboration.entity.Collaboration;
import com.uniConnect.collaboration.enums.CollaborationStatus;
import java.time.LocalDate;
import lombok.*;

@Getter
@Builder
public class CollaborationSummaryResponse {

    private Long id;               // 협업 ID
    private String universityName; // 학교명
    private String eventName;      // 캠페인명
    private LocalDate startDate;   // 행사 시작일
    private LocalDate endDate;     // 행사 종료일
    private Integer quantity;      // 제품 수량
    private String status;         // 협업 상태

    public static CollaborationSummaryResponse from(Collaboration c) {
        var matching = c.getMatching();
        var campaign = (matching != null) ? matching.getCampaign() : null;
        var studentOrg = (matching != null) ? matching.getStudentOrg() : null;

        return CollaborationSummaryResponse.builder()
                .id(c.getId())
                .universityName(studentOrg != null ? studentOrg.getSchoolName() : null) // ✅ 여기 수정!
                .eventName(campaign != null ? campaign.getName() : null)
                .startDate(campaign != null ? campaign.getStartDate() : null)
                .endDate(campaign != null ? campaign.getEndDate() : null)
                .quantity(campaign != null ? campaign.getProductQuantity() : null)
                .status(c.getStatus().name())
                .build();
    }
}
package com.uniConnect.collaboration.dto;

import com.uniConnect.collaboration.entity.Collaboration;
import com.uniConnect.collaboration.enums.CollaborationStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CollaborationDetailResponse {

    private Long collaborationId;
    private CollaborationStatus status;

    private String contractUrl;
    private LocalDateTime contractSentAt;
    private LocalDateTime studentSignedAt;
    private LocalDateTime adminContractApprovedAt;

    public static CollaborationDetailResponse from(Collaboration c) {
        return CollaborationDetailResponse.builder()
                .collaborationId(c.getId())
                .status(c.getStatus())
                .contractUrl(c.getContractUrl())
                .contractSentAt(c.getCreatedAt())
                .studentSignedAt(c.getStudentSignedAt())
                .adminContractApprovedAt(c.getAdminContractApprovedAt())
                .build();
    }
}

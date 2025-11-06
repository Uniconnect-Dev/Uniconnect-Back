package com.uniConnect.compliance.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComplianceAgreementResponse {

    private String requestId;
    private Long agreementId;
    private String status; // "COMPLETED", "PENDING", etc.
    private List<AgreementDetail> agreementDetails;
    private LocalDateTime agreedAt;
    private String message;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AgreementDetail {
        private String type;
        private Boolean accepted;
        private String description;
    }
}

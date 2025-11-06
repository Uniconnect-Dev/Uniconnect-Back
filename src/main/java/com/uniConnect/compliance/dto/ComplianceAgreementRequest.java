package com.uniConnect.compliance.dto;

import com.uniConnect.compliance.enums.AgreementType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComplianceAgreementRequest {

    @NotBlank(message = "requestId는 필수입니다")
    private String requestId;

    @NotEmpty(message = "최소 1개 이상의 동의 항목이 필요합니다")
    @Valid
    @Schema(
            description = "동의 항목 목록 (3개 항목 모두 필수)",
            required = true,
            example = """
            [
              {
                "type": "PROCESS_INFO",
                "accepted": true
              },
              {
                "type": "OFFPLATFORM_PENALTY",
                "accepted": true
              },
              {
                "type": "TERMS_ACK",
                "accepted": true
              }
            ]
            """)
    private List<AgreementItem> agreements;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AgreementItem {
        @NotNull(message = "동의 타입은 필수입니다")
        private AgreementType type;

        @NotNull(message = "동의 여부는 필수입니다")
        private Boolean accepted;
    }
}
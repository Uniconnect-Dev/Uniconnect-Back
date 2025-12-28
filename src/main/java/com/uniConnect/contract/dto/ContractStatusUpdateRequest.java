package com.uniConnect.contract.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContractStatusUpdateRequest {

    @Schema(
            description = "변경할 계약 상태",
            example = "ReceiptPending",
            allowableValues = {"PendingSignature", "StudentSigned", "Signed", "ReceiptPending", "ReceiptSigned"}
    )
    private String status;
}
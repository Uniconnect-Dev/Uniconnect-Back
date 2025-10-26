package com.uniConnect.contract.dto;

import com.uniConnect.contract.entity.Contract;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReceiptSignResponseDto {
    private Long contractId;
    private String status;
    private LocalDateTime receiptSignedAt;
    private String receiptSignatureFileUrl;
    private String message;

    public static ReceiptSignResponseDto fromEntity(Contract contract) {
        if (contract == null) return null;

        return ReceiptSignResponseDto.builder()
                .contractId(contract.getContractId())
                .status(contract.getStatus() != null ? contract.getStatus().name() : null)
                .receiptSignedAt(contract.getReceiptSignedAt())
                .receiptSignatureFileUrl(contract.getReceiptSignatureFileUrl())
                .message("전송이 완료되었습니다.")
                .build();
    }
}

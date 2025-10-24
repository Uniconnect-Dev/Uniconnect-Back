package com.uniConnect.contract.dto;

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
}

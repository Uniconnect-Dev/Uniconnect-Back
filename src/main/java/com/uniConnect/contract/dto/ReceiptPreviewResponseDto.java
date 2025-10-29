package com.uniConnect.contract.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReceiptPreviewResponseDto {
    private Long contractId;
    private String receiptPdfUrl;
    private String status;
}

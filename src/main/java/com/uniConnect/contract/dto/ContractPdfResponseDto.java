package com.uniConnect.contract.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContractPdfResponseDto {
    private Long contractId;
    private String pdfUrl; // 계약서 PDF S3 URL
}

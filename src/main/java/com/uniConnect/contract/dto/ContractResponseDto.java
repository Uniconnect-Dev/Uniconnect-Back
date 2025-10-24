package com.uniConnect.contract.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContractResponseDto {
    private Long contractId;

    private String status;

    private Boolean studentSigned;
    private Boolean companySigned;

    private LocalDateTime studentSignedAt;
    private LocalDateTime companySignedAt;

    private String pdfUrl;                    // 계약서 PDF
    private String signatureFileUrl;         // 학생 서명 이미지
    private String companySignatureFileUrl;  // 회사 서명 이미지

    private String receiptPdfUrl;            // 인수증 PDF
    private String receiptSignatureFileUrl;  // 인수증 서명 이미지
    private LocalDateTime receiptSignedAt;   // 인수증 서명 시각
}
package com.uniConnect.contract.dto;

import com.uniConnect.contract.entity.Contract;
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

    public static ContractResponseDto fromEntity(Contract contract) {
        if (contract == null) return null;

        return ContractResponseDto.builder()
                .contractId(contract.getContractId())
                .status(contract.getStatus() != null ? contract.getStatus().name() : null)
                .studentSigned(contract.getStudentSigned())
                .companySigned(contract.getCompanySigned())
                .studentSignedAt(contract.getStudentSignedAt())
                .companySignedAt(contract.getCompanySignedAt())
                .pdfUrl(contract.getPdfUrl())
                .signatureFileUrl(contract.getSignatureFileUrl())
                .companySignatureFileUrl(contract.getCompanySignatureFileUrl())
                .receiptPdfUrl(contract.getReceiptPdfUrl())
                .receiptSignatureFileUrl(contract.getReceiptSignatureFileUrl())
                .receiptSignedAt(contract.getReceiptSignedAt())
                .build();
    }
}

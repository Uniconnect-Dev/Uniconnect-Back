package com.uniConnect.contract.dto;

import com.uniConnect.contract.entity.Contract;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContractListItemDto {
    private Long contractId;

    private String studentOrgName;
    private String campaignName;
    private String collaborationType;

    private String status;
    private Boolean studentSigned;
    private Boolean companySigned;
    private LocalDateTime studentSignedAt;
    private LocalDateTime companySignedAt;

    private String pdfUrl;
    private String receiptPdfUrl;

    public static ContractListItemDto fromEntity(Contract contract) {
        if (contract == null) return null;

        String studentOrgName = null;
        String campaignName = null;
        String collaborationType = null;

        if (contract.getMatching() != null) {
            var matching = contract.getMatching();
            if (matching.getStudentOrg() != null) {
                studentOrgName = matching.getStudentOrg().getOrganizationName();
            }
            if (matching.getCampaign() != null) {
                campaignName = matching.getCampaign().getName();
                collaborationType = matching.getCampaign().getPurpose();
            }
        }

        return ContractListItemDto.builder()
                .contractId(contract.getContractId())
                .studentOrgName(studentOrgName)
                .campaignName(campaignName)
                .collaborationType(collaborationType)
                .status(contract.getStatus() != null ? contract.getStatus().name() : null)
                .studentSigned(contract.getStudentSigned())
                .companySigned(contract.getCompanySigned())
                .studentSignedAt(contract.getStudentSignedAt())
                .companySignedAt(contract.getCompanySignedAt())
                .pdfUrl(contract.getPdfUrl())
                .receiptPdfUrl(contract.getReceiptPdfUrl())
                .build();
    }
}

package com.uniConnect.contract.dto;

import com.uniConnect.contract.entity.Contract;
import com.uniConnect.collaboration.entity.Collaboration;
import com.uniConnect.matching.entity.CollaborationMatchRequest;
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

        Collaboration collab = contract.getCollaboration();
        if (collab != null) {

            CollaborationMatchRequest match = collab.getMatchRequest();
            if (match != null) {

                // 학생단체명
                if (match.getStudentOrg() != null) {
                    studentOrgName = match.getStudentOrg().getOrganizationName();
                }

                // 캠페인 정보
                if (match.getCampaign() != null) {
                    campaignName = match.getCampaign().getName();

                    if (match.getCampaign().getCollaborationType() != null) {
                        collaborationType =
                                match.getCampaign().getCollaborationType().name();
                    }
                }
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

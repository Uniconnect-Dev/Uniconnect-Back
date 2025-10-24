package com.uniConnect.contract.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContractListItemDto {
    private Long contractId;

    private String studentOrgName;   // 매칭에서 student_org 이름
    private String campaignName;     // 매칭에서 캠페인 이름
    private String collaborationType; // 협업 형태, 필요하면 MatchingRequest나 Campaign에 있는 필드 매핑

    private String status;           // PendingSignature / Singed / ...
    private Boolean studentSigned;
    private Boolean companySigned;
    private LocalDateTime studentSignedAt;
    private LocalDateTime companySignedAt;

    private String pdfUrl;           // 계약서 PDF S3 링크
    private String receiptPdfUrl;    // 인수증 PDF S3 링크 (없으면 null)
}

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
    private String pdfUrl;
    private String signatureFileUrl;
}


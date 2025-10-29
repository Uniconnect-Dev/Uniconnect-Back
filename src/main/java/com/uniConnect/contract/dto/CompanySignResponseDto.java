package com.uniConnect.contract.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanySignResponseDto {
    private Long contractId;
    private String status;
    private Boolean companySigned;
    private LocalDateTime companySignedAt;
    private String companySignatureFileUrl;
}

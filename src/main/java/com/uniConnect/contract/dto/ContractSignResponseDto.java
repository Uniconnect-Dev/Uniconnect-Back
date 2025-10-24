package com.uniConnect.contract.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContractSignResponseDto {
    private Long contractId;
    private String status;
    private Boolean studentSigned;
    private LocalDateTime studentSignedAt;
    private String signatureFileUrl;
}

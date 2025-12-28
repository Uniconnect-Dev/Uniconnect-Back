package com.uniConnect.contract.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanySignRequestDto {
    private String signatureBase64;
    private String signedAt;
}

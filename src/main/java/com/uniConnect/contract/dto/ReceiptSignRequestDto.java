package com.uniConnect.contract.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReceiptSignRequestDto {
    private String signatureBase64;
    private String signedAt;
}

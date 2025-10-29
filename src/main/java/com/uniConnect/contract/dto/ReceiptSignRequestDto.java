package com.uniConnect.contract.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReceiptSignRequestDto {
    private String signatureFileUrl; // 인수증 서명 이미지
}

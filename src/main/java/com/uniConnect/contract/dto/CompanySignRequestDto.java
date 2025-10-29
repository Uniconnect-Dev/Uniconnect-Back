package com.uniConnect.contract.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanySignRequestDto {
    private String signatureFileUrl; // 회사 쪽 서명 이미지 URL
}

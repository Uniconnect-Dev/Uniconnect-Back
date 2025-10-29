package com.uniConnect.contract.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContractSignRequestDto {

    // S3 업로드된 서명 이미지 파일의 URL 또는 파일 ID
    private String signatureFileUrl;
}

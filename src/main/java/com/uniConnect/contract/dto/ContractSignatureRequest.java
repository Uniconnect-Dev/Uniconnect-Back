package com.uniConnect.contract.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContractSignatureRequest {

    private String signatureBase64;
    private Long timestamp;
}

package com.uniConnect.contract.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ContractPdfDownloadResponse {

    private String downloadUrl;
}

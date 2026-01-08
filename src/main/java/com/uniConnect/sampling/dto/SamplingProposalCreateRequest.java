package com.uniConnect.sampling.dto;

import lombok.Getter;

import java.time.LocalDate;

@Getter
public class SamplingProposalCreateRequest {

    private String productName;
    private String industry;
    private String samplingPurpose;
    private LocalDate samplingStartDate;
    private LocalDate samplingEndDate;
    private Integer productCount;
    private String detailRequest;
}

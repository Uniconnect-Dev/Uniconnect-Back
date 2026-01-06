package com.uniConnect.sampling.dto;

import com.uniConnect.sampling.enums.IndustryType;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class SamplingProposalCreateRequest {

    private String productName;
    private IndustryType industry;
    private String samplingPurpose;
    private LocalDate samplingStartDate;
    private LocalDate samplingEndDate;
    private Integer productCount;
    private String detailRequest;
}

package com.uniConnect.sampling.dto;

import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SamplingMatchCondition {

    private String schoolName;
    private Integer verificationLevel;

    private String industry;         // 기업 산업군
    private String purpose;          // 캠페인 목적
    private LocalDate samplingDate;  // 원하는 시기
}

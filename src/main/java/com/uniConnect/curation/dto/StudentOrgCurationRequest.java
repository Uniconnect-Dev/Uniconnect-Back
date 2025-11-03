package com.uniConnect.curation.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.util.List;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentOrgCurationRequest {
    @JsonProperty("qTags")
    private List<String> qTags;            // 기업 요구 태그 세트

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate samplingStartDate;     // 샘플링 시작일

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate samplingEndDate; // 샘플링 종료일
    private Integer productQuantity;             // 제공 수량 (규모 매칭 판단용)
}
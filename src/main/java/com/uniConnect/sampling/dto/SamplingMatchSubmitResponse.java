package com.uniConnect.sampling.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class SamplingMatchSubmitResponse {

    private int requestedCount;
    private String message;

    public static SamplingMatchSubmitResponse of(int count) {
        return SamplingMatchSubmitResponse.builder()
                .requestedCount(count)
                .message(count + "개의 학생단체에 샘플링 요청을 보냈습니다.")
                .build();
    }
}

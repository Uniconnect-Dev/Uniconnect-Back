package com.uniConnect.sampling.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class EstimatedTotalCostResponse {
    private int minEstimatedTotal;
    private int maxEstimatedTotal;
    private String message;

    public static EstimatedTotalCostResponse of(int total) {

        int min = total - 100_000;
        int max = total + 100_000;

        return EstimatedTotalCostResponse.builder()
                .minEstimatedTotal(min)
                .maxEstimatedTotal(max)
                .message(
                        String.format(
                                "선택하신 단체들의 예상 이용료는 약 %,d원 ~ %,d원입니다. (확정 시 정확 금액 안내)",
                                min, max
                        )
                )
                .build();
    }
}

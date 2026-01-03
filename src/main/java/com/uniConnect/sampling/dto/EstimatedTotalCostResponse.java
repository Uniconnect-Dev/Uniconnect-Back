package com.uniConnect.sampling.dto;

import lombok.AllArgsConstructor;
import lombok.*;

@Getter
@AllArgsConstructor
public class EstimatedTotalCostResponse {

    private int minCost;
    private int maxCost;
    private String displayRange;

    public static EstimatedTotalCostResponse of(int baseCost) {
        return new EstimatedTotalCostResponse(
                baseCost - 100_000,
                baseCost + 100_000,
                String.format("%,d원 ~ %,d원",
                        baseCost - 100_000,
                        baseCost + 100_000)
        );
    }
}
package com.uniConnect.sampling.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class EstimatedTotalCostResponse {
    private int minEstimatedTotal;
    private int maxEstimatedTotal;
    private String message;
}

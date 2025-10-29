package com.uniConnect.collaboration.dto;

import lombok.*;
import java.time.LocalDate;

@Data
public class ProductInfoRequest {
    private Long collaborationId;
    private String productName;
    private Integer quantity;
    private String description;
}

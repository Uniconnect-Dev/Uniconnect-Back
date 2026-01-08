package com.uniConnect.shop.dto;

import lombok.*;

@Getter
@AllArgsConstructor
public class ProductListResponse {

    private Long companyId;
    private String companyName;

    private Long productId;
    private String productName;
    private Integer price;
    private String thumbnailUrl;
}

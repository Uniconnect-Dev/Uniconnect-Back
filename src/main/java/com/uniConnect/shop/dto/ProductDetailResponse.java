package com.uniConnect.shop.dto;

import lombok.*;

@Getter
@AllArgsConstructor
public class ProductDetailResponse {

    private Long companyId;
    private String companyName;
    private String industryName;

    private Long productId;
    private String productName;
    private Integer price;
    private String thumbnailUrl;
    private String detailImageUrl;
}

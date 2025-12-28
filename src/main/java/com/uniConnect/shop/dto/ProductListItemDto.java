package com.uniConnect.shop.dto;

import com.uniConnect.shop.entity.Product;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductListItemDto {

    private Long productId;
    private String companyName;
    private String name;
    private String priceFormatted;
    private String shortDescription;
    private String thumbnailUrl;

    public static ProductListItemDto fromEntity(Product p) {
        String price = String.format("%,d원", p.getPrice());

        return ProductListItemDto.builder()
                .productId(p.getProductId())
                .companyName(p.getCompany().getBrandName())
                .name(p.getName())
                .priceFormatted(price)
                .shortDescription(p.getShortDescription())
                .thumbnailUrl(
                        p.getThumbnailUrl() != null ?
                                p.getThumbnailUrl() :
                                p.getCompany().getLogoUrl()
                )
                .build();
    }
}

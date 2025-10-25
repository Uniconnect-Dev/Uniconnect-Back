package com.uniConnect.collaboration.dto;

import com.uniConnect.collaboration.entity.ProductInfo;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class ProductInfoResponse {
    private Long productInfoId;
    private Long collaborationId;
    private String productName;
    private Integer quantity;
    private String description;
    private String imageUrl;
    private String logoUrl;
    private LocalDate shippingDate;
    private Boolean isShipped;
    private String trackingNo;
    private String providedBy;
    private LocalDateTime createdAt;

    public static ProductInfoResponse from(ProductInfo entity) {
        return ProductInfoResponse.builder()
                .productInfoId(entity.getProductInfoId())
                .collaborationId(entity.getCollaboration().getId())
                .productName(entity.getProductName())
                .quantity(entity.getQuantity())
                .description(entity.getDescription())
                .imageUrl(entity.getImageUrl())
                .logoUrl(entity.getLogoUrl())
                .shippingDate(entity.getDeliveryDate())
                .isShipped(entity.getIsShipped())
                .trackingNo(entity.getTrackingNo())
                .providedBy(entity.getProvidedBy() != null ? entity.getProvidedBy().name() : null)
                .createdAt(entity.getCreatedAt())
                .build();
    }
}

package com.uniConnect.collaboration.dto;

import com.uniConnect.collaboration.entity.ContentUpload;
import com.uniConnect.collaboration.entity.ProductInfo;
import com.uniConnect.collaboration.entity.ShippingInfo;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanySectionDto {

    // 기업이 공유한 제품 정보
    private List<ProductBlock> products;

    // 기업이 올린 이미지들 (제품 사진, 로고 등)
    private List<UploadBlock> uploads;

    // 발송 정보
    private ShippingBlock shipping;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductBlock {
        private String productName;
        private Integer quantity;
        private String description;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UploadBlock {
        private String imageUrl;
        private String caption;
        private LocalDateTime uploadedAt;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShippingBlock {
        private LocalDate shippingDate;
        private String trackingNumber;
        private Boolean isShipped;
        private String note;
    }

    public static CompanySectionDto of(
            List<ProductInfo> products,
            List<ContentUpload> uploads,
            ShippingInfo shippingInfo
    ) {
        return CompanySectionDto.builder()
                .products(
                        products.stream()
                                .map(p -> ProductBlock.builder()
                                        .productName(p.getProductName())
                                        .quantity(p.getQuantity())
                                        .description(p.getDescription())
                                        .build()
                                ).toList()
                )
                .uploads(
                        uploads.stream()
                                // 기업이 올린 것만
                                .filter(u -> u.getUploaderType() != null && u.getUploaderType().name().equals("COMPANY"))
                                .map(u -> UploadBlock.builder()
                                        .imageUrl(u.getImageUrl())
                                        .caption(u.getCaption())
                                        .uploadedAt(u.getUploadedAt())
                                        .build()
                                ).toList()
                )
                .shipping(shippingInfo == null ? null :
                        ShippingBlock.builder()
                                .shippingDate(shippingInfo.getShippingDate())
                                .trackingNumber(shippingInfo.getTrackingNumber())
                                .isShipped(shippingInfo.getIsShipped())
                                .note(shippingInfo.getNote())
                                .build()
                )
                .build();
    }
}
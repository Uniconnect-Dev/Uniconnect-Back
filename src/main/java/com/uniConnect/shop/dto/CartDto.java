package com.uniConnect.shop.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

public class CartDto {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddToCartRequest {
        @NotNull(message = "Product ID는 필수입니다")
        private Long productId;

        @NotNull(message = "수량은 필수입니다")
        @Min(value = 1, message = "수량은 1 이상이어야 합니다")
        private Integer quantity;
    }

    @Data
    @Builder
    public static class CartItemResponse {
        private Long cartItemId;
        private Long productId;
        private String productName;
        private Integer unitPrice;
        private Integer quantity;
        private Integer subtotal;
    }

    @Data
    @Builder
    public static class CartResponse {
        private Long cartId;
        private Long studentOrgId;
        private List<CartItemResponse> items;
        private Integer totalAmount;
        private Integer totalItemCount;
    }
}
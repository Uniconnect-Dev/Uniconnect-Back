package com.uniConnect.shop.controller;

import com.uniConnect.shop.dto.CartDto;
import com.uniConnect.shop.service.CartService;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/carts")
@RequiredArgsConstructor
@Tag(name = "Cart Management", description = "학생단체 장바구니 관리 API")
public class CartController {

    private final CartService cartService;

    /**
     * 장바구니 조회
     */
    @GetMapping("/{studentOrgId}")
    @Operation(summary = "장바구니 조회",
            description = "학생단체별 장바구니 정보를 조회합니다. 장바구니가 없으면 자동으로 생성됩니다.",
            tags = {"Cart Management"})
    public ResponseEntity<CartDto.CartResponse> getCart(@PathVariable Long studentOrgId) {
        CartDto.CartResponse response = cartService.getCart(studentOrgId);
        return ResponseEntity.ok(response);
    }

    /**
     * 장바구니에 상품 추가
     */
    @PostMapping("/{studentOrgId}/items")
    @Operation(
            summary = "장바구니에 상품 추가",
            description = "장바구니에 새로운 상품을 추가하거나, 기존 상품의 수량을 증가시킵니다. " +
                    "동일 상품이 이미 존재하면 수량이 누적됩니다.",
            tags = {"Cart Management"}
    )
    public ResponseEntity<CartDto.CartResponse> addToCart(
            @Parameter(
                    name = "studentOrgId",
                    description = "학생단체 ID",
                    required = true,
                    example = "1"
            )
            @PathVariable Long studentOrgId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "장바구니에 추가할 상품 정보",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CartDto.AddToCartRequest.class))
            )
            @RequestBody @Valid CartDto.AddToCartRequest request) {
        CartDto.CartResponse response = cartService.addItemToCart(studentOrgId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 장바구니 항목 제거
     */
    @DeleteMapping("/{studentOrgId}/items/{cartItemId}")
    @Operation(
            summary = "장바구니 항목 제거",
            description = "장바구니에서 특정 상품을 제거합니다. 권한 검증을 통해 다른 학생단체의 장바구니는 수정할 수 없습니다.",
            tags = {"Cart Management"}
    )
    public ResponseEntity<CartDto.CartResponse> removeFromCart(
            @Parameter(
                    name = "studentOrgId",
                    description = "학생단체 ID",
                    required = true,
                    example = "1"
            )
            @PathVariable Long studentOrgId,
            @Parameter(
                    name = "cartItemId",
                    description = "제거할 장바구니 항목 ID",
                    required = true,
                    example = "5"
            )
            @PathVariable Long cartItemId) {
        CartDto.CartResponse response = cartService.removeItemFromCart(studentOrgId, cartItemId);
        return ResponseEntity.ok(response);
    }

    /**
     * 장바구니 항목 수량 수정
     */
    @PutMapping("/{studentOrgId}/items/{cartItemId}")
    @Operation(
            summary = "장바구니 항목 수량 수정",
            description = "장바구니 항목의 수량을 변경합니다. 수량은 1 이상이어야 합니다.",
            tags = {"Cart Management"}
    )
    public ResponseEntity<CartDto.CartResponse> updateQuantity(
            @Parameter(
                    name = "studentOrgId",
                    description = "학생단체 ID",
                    required = true,
                    example = "1"
            )
            @PathVariable Long studentOrgId,
            @Parameter(
                    name = "cartItemId",
                    description = "수정할 장바구니 항목 ID",
                    required = true,
                    example = "5"
            )
            @PathVariable Long cartItemId,
            @Parameter(
                    name = "quantity",
                    description = "변경할 수량 (1 이상)",
                    required = true,
                    example = "5"
            )
            @RequestParam Integer quantity) {
        CartDto.CartResponse response = cartService.updateItemQuantity(studentOrgId, cartItemId, quantity);
        return ResponseEntity.ok(response);
    }

    /**
     * 장바구니 전체 초기화
     */
    @DeleteMapping("/{studentOrgId}")
    @Operation(
            summary = "장바구니 전체 초기화",
            description = "학생단체의 장바구니에 담긴 모든 상품을 제거합니다. " +
                    "결제 완료 후 자동으로 호출되거나, 사용자가 수동으로 초기화할 수 있습니다.",
            tags = {"Cart Management"}
    )
    public ResponseEntity<Void> clearCart(@Parameter(
            name = "studentOrgId",
            description = "학생단체 ID",
            required = true,
            example = "1"
    ) @PathVariable Long studentOrgId) {
        cartService.clearCart(studentOrgId);
        return ResponseEntity.noContent().build();
    }
}
package com.uniConnect.shop.controller;

import com.uniConnect.global.response.ApiResponse;
import com.uniConnect.shop.dto.ProductListItemDto;
import com.uniConnect.shop.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shop/products")
@RequiredArgsConstructor
@Tag(name = "Shop Product API", description = "제휴 기업 제품 목록 조회 API")
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "제휴 기업 제품 목록 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductListItemDto>>> getProductList() {
        List<ProductListItemDto> products = productService.getProductList();

        if (products.isEmpty()) {
            return ResponseEntity.ok(
                    ApiResponse.success("아직 등록된 제품이 없어요.", products)
            );
        }

        return ResponseEntity.ok(
                ApiResponse.success("제품 목록 조회 성공", products)
        );
    }
}

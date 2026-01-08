package com.uniConnect.shop.controller;

import com.uniConnect.shop.dto.CompanyProductListResponse;
import com.uniConnect.shop.dto.ProductDetailResponse;
import com.uniConnect.shop.dto.ProductListResponse;
import com.uniConnect.shop.service.ProductService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
@Tag(name = "Shop Product", description = "쇼핑몰 제품 조회 API")
public class ProductController {

    private final ProductService productService;

    /**
     * 쇼핑몰 전체 제품 조회
     */
    @Operation(
            summary = "쇼핑몰 제품 목록 조회",
            description = "제품 목록을 페이지 단위로 조회합니다. (카테고리 선택 시 필터링, 미선택 또는 '전체' 선택 시 전체 조회)"
    )
    @GetMapping
    public Page<ProductListResponse> getProducts(
            @Parameter(
                    description = "제품 카테고리 (전체, FNB, BEAUTY, EDUCATION, TRAVEL, ONLINE_SERVICE, STATIONERY, IT_TELECOM, ETC)",
                    example = "FNB"
            )
            @RequestParam(required = false) String category,

            @Parameter(
                    description = "페이지 번호 (0부터 시작)",
                    example = "0"
            )
            @RequestParam(defaultValue = "0") int page
    ) {
        return productService.getProducts(category, page);
    }

    /**
     * 기업 상세 제품 목록 조회
     */
    @Operation(
            summary = "기업별 제품 목록 조회",
            description = "특정 기업이 등록한 제품 목록을 페이지 단위로 조회합니다."
    )
    @GetMapping("/company/{companyId}")
    public Page<CompanyProductListResponse> getCompanyProducts(
            @Parameter(description = "회사 ID", example = "1")
            @PathVariable Long companyId,

            @Parameter(
                    description = "페이지 번호 (0부터 시작)",
                    example = "0"
            )
            @RequestParam(defaultValue = "0") int page
    ) {
        return productService.getCompanyProducts(companyId, page);
    }

    /**
     * 제품 상세 조회
     */
    @Operation(
            summary = "제품 상세 조회",
            description = "제품 ID로 제품 상세 정보를 조회합니다."
    )
    @GetMapping("/{productId}")
    public ProductDetailResponse getProductDetail(
            @Parameter(description = "제품 ID", example = "10")
            @PathVariable Long productId
    ) {
        return productService.getProductDetail(productId);
    }
}
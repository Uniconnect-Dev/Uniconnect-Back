package com.uniConnect.shop.service;

import com.uniConnect.shop.dto.*;
import com.uniConnect.shop.enums.ProductCategory;
import com.uniConnect.shop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public List<ProductListItemDto> getProductList() {
        return productRepository.findAllWithCompanyOrderByCreatedAtDesc()
                .stream()
                .map(ProductListItemDto::fromEntity)
                .toList();
    }

    // 쇼핑몰 제품 조회
    public Page<ProductListResponse> getProducts(
            String category,
            int page
    ) {
        Pageable pageable = PageRequest.of(page, 50);

        ProductCategory productCategory =
                "전체".equals(category) || category == null
                        ? null
                        : ProductCategory.valueOf(category);

        return productRepository.findProducts(productCategory, pageable);
    }

    // 기업 상세 제품 조회
    public Page<CompanyProductListResponse> getCompanyProducts(
            Long companyId,
            int page
    ) {
        Pageable pageable = PageRequest.of(page, 50);
        return productRepository.findByCompanyId(companyId, pageable);
    }

    // 제품 상세 조회
    public ProductDetailResponse getProductDetail(Long productId) {
        return productRepository.findProductDetail(productId);
    }
}

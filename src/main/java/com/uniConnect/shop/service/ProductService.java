package com.uniConnect.shop.service;

import com.uniConnect.shop.dto.ProductListItemDto;
import com.uniConnect.shop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
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
}

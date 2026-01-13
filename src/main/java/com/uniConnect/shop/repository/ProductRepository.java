package com.uniConnect.shop.repository;

import com.uniConnect.shop.entity.Product;
import com.uniConnect.shop.enums.ProductCategory;
import com.uniConnect.shop.dto.ProductListResponse;
import com.uniConnect.shop.dto.CompanyProductListResponse;
import com.uniConnect.shop.dto.ProductDetailResponse;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("SELECT p FROM Product p JOIN FETCH p.company ORDER BY p.createdAt DESC")
    List<Product> findAllWithCompanyOrderByCreatedAtDesc();

    //모든 product
    List<Product> findByCompanyCompanyId(Long companyId);

    //상세조회
    @Query("SELECT p FROM Product p JOIN FETCH p.company WHERE p.productId = :productId")
    Optional<Product> findByIdWithCompany(@Param("productId") Long productId);

    // 쇼핑몰 전체 제품 조회
    @Query("""
        select new com.uniConnect.shop.dto.ProductListResponse(
            c.companyId,
            c.brandName,
            p.productId,
            p.name,
            p.price,
            p.thumbnailUrl
        )
        from Product p
        join p.company c
        where (:category is null or p.category = :category)
    """)
    Page<ProductListResponse> findProducts(
            @Param("category") ProductCategory category,
            Pageable pageable
    );

    // 기업 상세 제품 목록 조회
    @Query("""
        select new com.uniConnect.shop.dto.CompanyProductListResponse(
            c.companyId,
            c.brandName,
            i.name,
            p.productId,
            p.name,
            p.price,
            p.thumbnailUrl
        )
        from Product p
        join p.company c
        join c.industry i
        where c.companyId = :companyId
    """)
    Page<CompanyProductListResponse> findByCompanyId(
            @Param("companyId") Long companyId,
            Pageable pageable
    );

    // 제품 상세 조회
    @Query("""
        select new com.uniConnect.shop.dto.ProductDetailResponse(
            c.companyId,
            c.brandName,
            i.name,
            p.productId,
            p.name,
            p.price,
            p.thumbnailUrl,
            p.detailImageUrl
        )
        from Product p
        join p.company c
        join c.industry i
        where p.productId = :productId
    """)
    ProductDetailResponse findProductDetail(
            @Param("productId") Long productId
    );
}

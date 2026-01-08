package com.uniConnect.shop.repository;

import com.uniConnect.shop.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

}

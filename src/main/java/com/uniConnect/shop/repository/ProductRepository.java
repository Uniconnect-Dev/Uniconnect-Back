package com.uniConnect.shop.repository;

import com.uniConnect.shop.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("SELECT p FROM Product p JOIN FETCH p.company ORDER BY p.createdAt DESC")
    List<Product> findAllWithCompanyOrderByCreatedAtDesc();
}

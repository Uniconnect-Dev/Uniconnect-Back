package com.uniConnect.collaboration.repository;

import com.uniConnect.collaboration.entity.ProductInfo;
import com.uniConnect.collaboration.entity.Collaboration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductInfoRepository extends JpaRepository<ProductInfo, Long> {
    List<ProductInfo> findByCollaboration(Collaboration collaboration);
}

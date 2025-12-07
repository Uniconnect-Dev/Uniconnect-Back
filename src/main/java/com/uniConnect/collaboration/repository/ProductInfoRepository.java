package com.uniConnect.collaboration.repository;

import com.uniConnect.collaboration.entity.ProductInfo;
import com.uniConnect.collaboration.entity.Collaboration;
import org.springframework.data.jpa.repository.JpaRepository;
import com.uniConnect.collaboration.enums.UploaderType;

import java.util.List;
import java.util.Optional;

public interface ProductInfoRepository extends JpaRepository<ProductInfo, Long> {
    List<ProductInfo> findByCollaboration(Collaboration collaboration);
    Optional<ProductInfo> findTopByCollaborationOrderByCreatedAtDesc(Collaboration collaboration);
    List<ProductInfo> findAllByProvidedByOrderByCreatedAtDesc(UploaderType providedBy);
}

package com.uniConnect.collaboration.repository;

import com.uniConnect.collaboration.entity.Collaboration;
import com.uniConnect.collaboration.entity.ShippingInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShippingInfoRepository extends JpaRepository<ShippingInfo, Long> {
    Optional<ShippingInfo> findByCollaboration(Collaboration collaboration);
}
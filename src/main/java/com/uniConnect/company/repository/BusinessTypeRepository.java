package com.uniConnect.company.repository;

import com.uniConnect.company.entity.BusinessType;
import com.uniConnect.company.enums.BusinessTypeEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BusinessTypeRepository extends JpaRepository<BusinessType, Long> {
    Optional<BusinessType> findByName(String name);
    Optional<BusinessType> findByType(BusinessTypeEnum type);
}
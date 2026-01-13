package com.uniConnect.company.repository;

import com.uniConnect.company.entity.Industry;
import com.uniConnect.company.enums.IndustryType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IndustryRepository extends JpaRepository<Industry, Long> {
    Optional<Industry> findByType(IndustryType type);
}
package com.uniConnect.company.repository;

import com.uniConnect.company.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, Long> {
    Optional<Company> findByUser_UserId(Long userId);
    boolean existsByUser_UserId(Long userId);
}
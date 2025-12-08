package com.uniConnect.company.repository;

import com.uniConnect.company.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, Long> {

    @Query("""
        SELECT c
        FROM Company c
        LEFT JOIN FETCH c.industry i
        LEFT JOIN FETCH CompanyProfile p ON p.company.companyId = c.companyId
        """)
    List<Company> findAllWithProfileAndIndustry();

    @Query("""
        SELECT DISTINCT c
        FROM Company c
        LEFT JOIN FETCH c.users u
        """)
    List<Company> findAllWithUsers();

    Optional<Company> findByUsers_UserId(Long userId);

    Optional<Company> findByMainContactId(Long mainContactId);

    boolean existsByUsers_UserId(Long userId);
}
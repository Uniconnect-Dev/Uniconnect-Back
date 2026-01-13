package com.uniConnect.matching.repository;

import com.uniConnect.matching.entity.CompanyMatchingStatistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompanyMatchingStatisticsRepository extends JpaRepository<CompanyMatchingStatistics, Long> {
    Optional<CompanyMatchingStatistics> findByCompanyId(Long companyId);
}
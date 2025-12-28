package com.uniConnect.company.repository;

import com.uniConnect.company.entity.Company;
import com.uniConnect.company.entity.CompanyProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompanyProfileRepository extends JpaRepository<CompanyProfile, Long> {

    Optional<CompanyProfile> findByCompanyCompanyId(Long companyId);

    Optional<CompanyProfile> findFirstByCompany(Company company);
}
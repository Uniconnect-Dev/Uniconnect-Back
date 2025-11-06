package com.uniConnect.compliance.repository;

import com.uniConnect.compliance.entity.ComplianceAgreement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ComplianceAgreementRepository extends JpaRepository<ComplianceAgreement, Long> {
    Optional<ComplianceAgreement> findByRequestId(String requestId);
    boolean existsByRequestId(String requestId);
}
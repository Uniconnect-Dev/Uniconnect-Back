package com.uniConnect.compliance.repository;

import com.uniConnect.compliance.entity.ComplianceAgreementDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComplianceAgreementDetailRepository extends JpaRepository<ComplianceAgreementDetail, Long> {
    List<ComplianceAgreementDetail> findByAgreement_AgreementId(Long agreementId);
}
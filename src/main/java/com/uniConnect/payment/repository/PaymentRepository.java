package com.uniConnect.payment.repository;

import com.uniConnect.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByCompanyCompanyIdOrderByCreatedAtDesc(Long CompanyId);
    Optional<Payment> findByStudentOrgStudentOrgIdOrderByCreatedAtDesc(Long CompanyId);
    Optional<Payment> findByCampaignCampaignIdOrderByCreatedAtDesc(Long CompanyId);
}

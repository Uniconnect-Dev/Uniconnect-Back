package com.uniConnect.payment.repository;

import com.uniConnect.payment.entity.Payment;
import com.uniConnect.payment.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByCompanyCompanyIdOrderByCreatedAtDesc(Long CompanyId);
    Optional<Payment> findByStudentOrgStudentOrgIdOrderByCreatedAtDesc(Long CompanyId);
    // CollaborationMatchRequest 기반 조회
    Optional<Payment> findByCollaborationMatchRequest_Id(Long matchRequestId);

    // 상태별 결제 조회
    List<Payment> findByCompanyCompanyIdAndStatus(Long companyId, PaymentStatus status);
    List<Payment> findByStudentOrgStudentOrgIdAndStatus(Long studentOrgId, PaymentStatus status);


}

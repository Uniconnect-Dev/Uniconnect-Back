package com.uniConnect.payment.repository;

import com.uniConnect.payment.entity.RefundRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RefundRequestRepository extends JpaRepository<RefundRequest, Long> {
    /**
     * 기업별 환불 요청 목록 조회 (최신순)
     */
    List<RefundRequest> findByPaymentCompanyCompanyIdOrderByRequestedAtDesc(Long companyId);
    List<RefundRequest> findByPaymentStudentOrgStudentOrgIdOrderByRequestedAtDesc(Long companyId);

}

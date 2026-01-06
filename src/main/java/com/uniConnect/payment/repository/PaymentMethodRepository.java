package com.uniConnect.payment.repository;

import com.uniConnect.payment.entity.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Long> {
    // 회사, 학생단체의 모든 결제 수단 조회
    List<PaymentMethod> findByCompanyCompanyId(Long companyId);
    List<PaymentMethod> findByStudentOrgStudentOrgId(Long companyId);

    // 특정 결제 수단 조회 (권한 검증용)
    Optional<PaymentMethod> findByMethodIdAndCompanyCompanyId(Long methodId, Long companyId);
    Optional<PaymentMethod> findByMethodIdAndStudentOrgStudentOrgId(Long methodId, Long companyId);

}

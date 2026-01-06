package com.uniConnect.member.repository;

import com.uniConnect.member.entity.BusinessRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BusinessRegistrationRepository extends JpaRepository<BusinessRegistration, Long> {
    // Company ID로 조회
    Optional<BusinessRegistration> findByCompanyCompanyId(Long companyId);

    // User ID로 조회 (기존)
    Optional<BusinessRegistration> findByUserUserId(Long userId);

    // 사업자등록번호로 조회
    Optional<BusinessRegistration> findByRegistrationNo(String registrationNo);

}
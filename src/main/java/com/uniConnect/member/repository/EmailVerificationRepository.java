package com.uniConnect.member.repository;

import com.uniConnect.member.entity.BusinessRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmailVerificationRepository extends JpaRepository<BusinessRegistration, Long> {
}
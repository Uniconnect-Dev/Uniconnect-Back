package com.uniConnect.member.repository;

import com.uniConnect.member.entity.BusinessRegistration;
import com.uniConnect.member.entity.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {
    // 이메일로 최신 인증 레코드 조회
    Optional<EmailVerification> findByEmail(String email);

    // 이메일 존재 여부
    boolean existsByEmail(String email);

    // 이메일 삭제 (가입 완료 후)
    void deleteByEmail(String email);
}
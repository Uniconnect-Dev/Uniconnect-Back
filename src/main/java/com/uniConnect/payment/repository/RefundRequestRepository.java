package com.uniConnect.payment.repository;

import com.uniConnect.payment.entity.RefundRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefundRequestRepository extends JpaRepository<RefundRequest, Long> {
}

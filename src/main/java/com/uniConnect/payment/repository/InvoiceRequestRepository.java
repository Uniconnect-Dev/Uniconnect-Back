package com.uniConnect.payment.repository;

import com.uniConnect.payment.entity.InvoiceRequest;
import com.uniConnect.payment.enums.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;


public interface InvoiceRequestRepository extends JpaRepository<InvoiceRequest, Long> {
    List<InvoiceRequest> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<InvoiceRequest> findByPaymentId(Long paymentId);

    List<InvoiceRequest> findByStatus(InvoiceStatus status);
}

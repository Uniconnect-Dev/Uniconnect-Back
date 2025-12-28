package com.uniConnect.invoice.repository;

import com.uniConnect.invoice.entity.InvoiceRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;


public interface InvoiceRequestRepository extends JpaRepository<InvoiceRequest, Long> {
    List<InvoiceRequest> findByUserId(Long userId);
}

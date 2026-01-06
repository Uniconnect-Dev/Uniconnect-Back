package com.uniConnect.invoice.repository;

import com.uniConnect.company.entity.Company;
import com.uniConnect.invoice.entity.InvoiceRequest;
import com.uniConnect.invoice.entity.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;


public interface InvoiceRequestRepository extends JpaRepository<InvoiceRequest, Long> {
    List<InvoiceRequest> findByUserId(Long userId);
    Optional<InvoiceRequest> findByCompanyNameAndStatus(String companyName, InvoiceStatus status);

}

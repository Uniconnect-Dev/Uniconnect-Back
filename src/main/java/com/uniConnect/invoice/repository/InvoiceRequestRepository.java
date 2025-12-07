package com.uniConnect.invoice.repository;

import com.uniConnect.invoice.entity.InvoiceRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvoiceRequestRepository extends JpaRepository<InvoiceRequest, Long> { }

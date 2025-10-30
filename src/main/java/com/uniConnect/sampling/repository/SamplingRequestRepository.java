package com.uniConnect.sampling.repository;

import com.uniConnect.sampling.entity.SamplingRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SamplingRequestRepository extends JpaRepository<SamplingRequest, Long> {}
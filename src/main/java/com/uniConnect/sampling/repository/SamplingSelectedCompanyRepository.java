package com.uniConnect.sampling.repository;

import com.uniConnect.sampling.entity.SamplingSelectedCompany;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SamplingSelectedCompanyRepository extends JpaRepository<SamplingSelectedCompany, Long> {

    List<SamplingSelectedCompany> findBySamplingRequest_SamplingRequestId(Long samplingRequestId);

    void deleteBySamplingRequest_SamplingRequestId(Long samplingRequestId);
}

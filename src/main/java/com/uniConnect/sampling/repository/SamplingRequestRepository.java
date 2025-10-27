package com.uniConnect.sampling.repository;

import com.uniConnect.member.entity.User;
import com.uniConnect.sampling.entity.SamplingRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SamplingRequestRepository extends JpaRepository<SamplingRequest, Long> {
    Optional<SamplingRequest> findBySamplingRequestIdAndUser(Long samplingRequestId, User user);
}
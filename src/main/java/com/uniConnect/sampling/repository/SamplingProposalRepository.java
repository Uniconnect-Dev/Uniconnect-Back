package com.uniConnect.sampling.repository;

import com.uniConnect.sampling.entity.SamplingProposal;
import org.springframework.data.jpa.repository.JpaRepository;
import com.uniConnect.sampling.enums.SamplingStatus;
import java.util.List;

public interface SamplingProposalRepository
        extends JpaRepository<SamplingProposal, Long> {
    List<SamplingProposal> findAllByStatus(SamplingStatus status);
    List<SamplingProposal> findByStatus(SamplingStatus status);

}

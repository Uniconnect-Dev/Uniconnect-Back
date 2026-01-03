package com.uniConnect.partnership.repository;

import com.uniConnect.partnership.entity.CollaborationProposal;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CollaborationProposalRepository
        extends JpaRepository<CollaborationProposal, Long> {
}

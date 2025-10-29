package com.uniConnect.collaboration.repository;

import com.uniConnect.collaboration.entity.Collaboration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CollaborationRepository extends JpaRepository<Collaboration, Long> {
    Optional<Collaboration> findByMatching_MatchingId(Long matchingId);
}

package com.uniConnect.collaboration.repository;

import com.uniConnect.collaboration.entity.Collaboration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CollaborationRepository extends JpaRepository<Collaboration, Long> {

    Optional<Collaboration> findByMatching_MatchingId(Long matchingId);

    @Query("""
        SELECT c FROM Collaboration c
        LEFT JOIN FETCH c.matching m
        LEFT JOIN FETCH m.campaign ca
        LEFT JOIN FETCH m.studentOrg so
    """)
    List<Collaboration> findAllWithDetails();
}

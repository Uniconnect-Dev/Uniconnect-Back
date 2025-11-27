package com.uniConnect.campaign.repository;

import com.uniConnect.campaign.entity.MatchingRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MatchingRequestRepository extends JpaRepository<MatchingRequest, Long> {

    @Query("""
        SELECT COUNT(m) > 0
        FROM MatchingRequest m
        JOIN m.studentOrg so
        JOIN so.users u
        WHERE m.matchingId = :matchingId
          AND u.userId = :userId
    """)
    boolean existsByMatchingIdAndStudentOrgUsers(
            @Param("matchingId") Long matchingId,
            @Param("userId") Long userId
    );

    @Query("""
    SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END
    FROM MatchingRequest m
    JOIN m.campaign c
    JOIN c.company comp
    JOIN comp.users u
    WHERE m.matchingId = :matchingId
      AND u.userId = :userId
""")
    boolean existsByMatchingIdAndCompanyUsers(
            @Param("matchingId") Long matchingId,
            @Param("userId") Long userId
    );
}
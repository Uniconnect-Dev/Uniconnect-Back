package com.uniConnect.sampling.repository;

import com.uniConnect.sampling.entity.SamplingRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SamplingRequestRepository extends JpaRepository<SamplingRequest, Long> {
    Optional<SamplingRequest> findTopByUser_UserIdOrderByCreatedAtDesc(Long userId);


        @Query("""
        SELECT sr.samplingRequestId
        FROM SamplingRequest sr
        WHERE sr.user.userId = :userId
        ORDER BY sr.createdAt DESC
        """)
        List<Long> findLatestIdByUserId(@Param("userId") Long userId);

        @Query("""
        SELECT sr
        FROM SamplingRequest sr
        LEFT JOIN FETCH sr.selections s
        WHERE sr.samplingRequestId = :id
        """)
        Optional<SamplingRequest> findWithSelections(@Param("id") Long id);

}
package com.uniConnect.report.repository;

import com.uniConnect.report.entity.SamplingReport;
import com.uniConnect.report.enums.SamplingReportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface SamplingReportRepository extends JpaRepository<SamplingReport, Long>, SamplingReportRepositoryCustom {

    @Query("""
        SELECT sr
        FROM SamplingReport sr
        JOIN sr.campaign c
        JOIN c.company comp
        JOIN comp.users u
        WHERE u.userId = :userId
        AND (:status IS NULL OR sr.status = :status)
        AND sr.createdAt BETWEEN :start AND :end
        ORDER BY sr.createdAt DESC
    """)
    Page<SamplingReport> findReports(
            @Param("userId") Long userId,
            @Param("status") SamplingReportStatus status,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            Pageable pageable
    );


    @Query("""
        SELECT sr
        FROM SamplingReport sr
        JOIN sr.campaign c
        JOIN c.company comp
        JOIN comp.users u
        WHERE u.userId = :userId
        AND sr.createdAt BETWEEN :start AND :end
        ORDER BY sr.createdAt DESC
    """)
    Page<SamplingReport> findReportsByCompanyUserId(
            @Param("userId") Long userId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            Pageable pageable
    );
}

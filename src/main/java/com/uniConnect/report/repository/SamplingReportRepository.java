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
    SELECT r 
    FROM SamplingReport r
    JOIN r.campaign c
    JOIN c.studentOrg so
    JOIN so.users u
    WHERE u.userId = :userId
      AND (:status IS NULL OR r.status = :status)
      AND (:start IS NULL OR r.createdAt >= :start)
      AND (:end IS NULL OR r.createdAt <= :end)
""")
    Page<SamplingReport> findReports(
            @Param("userId") Long userId,
            @Param("status") SamplingReportStatus status,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            Pageable pageable
    );
}

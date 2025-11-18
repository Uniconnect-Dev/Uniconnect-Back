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
select r from SamplingReport r
join r.campaign c
join c.studentOrg so
join so.users u
where u.userId = :userId
  and (r.status = coalesce(:status, r.status))
  and (r.createdAt >= coalesce(:start, r.createdAt))
  and (r.createdAt <= coalesce(:end, r.createdAt))
""")
    Page<SamplingReport> findReports(
            Long userId,
            SamplingReportStatus status,
            LocalDateTime start,
            LocalDateTime end,
            Pageable pageable
    );
}

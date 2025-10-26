package com.uniConnect.report.repository;

import com.uniConnect.report.entity.SamplingReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SamplingReportRepository extends JpaRepository<SamplingReport, Long>, SamplingReportRepositoryCustom {
}

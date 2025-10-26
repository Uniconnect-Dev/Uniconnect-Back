package com.uniConnect.report.repository;

import com.uniConnect.report.entity.SamplingReportMedia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SamplingReportMediaRepository extends JpaRepository<SamplingReportMedia, Long> {

    List<SamplingReportMedia> findByReport_ReportId(Long reportId);
}

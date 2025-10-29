package com.uniConnect.report.repository;

import com.uniConnect.report.entity.SamplingReportFeedback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SamplingReportFeedbackRepository extends JpaRepository<SamplingReportFeedback, Long> {

    List<SamplingReportFeedback> findByReport_ReportId(Long reportId);
}

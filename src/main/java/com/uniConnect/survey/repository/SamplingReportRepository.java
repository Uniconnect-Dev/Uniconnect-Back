package com.uniConnect.survey.repository;

import com.uniConnect.survey.entity.SamplingReport;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SamplingReportRepository extends JpaRepository<SamplingReport, Long> {
    Optional<SamplingReport> findBySurvey_SurveyId(Long surveyId);
}

package com.uniConnect.survey.repository;

import com.uniConnect.survey.entity.SurveyReport;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SurveySamplingReportRepository extends JpaRepository<SurveyReport, Long> {
    Optional<SurveyReport> findBySurvey_SurveyId(Long surveyId);
}

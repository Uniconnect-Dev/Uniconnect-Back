package com.uniConnect.survey.repository;

import com.uniConnect.survey.entity.Survey;
import com.uniConnect.survey.entity.SurveyResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SurveyResponseRepository extends JpaRepository<SurveyResponse, Long> {
    List<SurveyResponse> findBySurvey(Survey survey);
}
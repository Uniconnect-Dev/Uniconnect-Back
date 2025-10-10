package com.uniConnect.survey.repository;

import com.uniConnect.survey.entity.Survey;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SurveyRepository extends JpaRepository<Survey, Long> {
    List<Survey> findByStudentOrg_StudentOrgId(Long studentOrgId);
}

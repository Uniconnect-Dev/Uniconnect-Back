package com.uniConnect.survey.repository;

import com.uniConnect.survey.entity.Survey;
import com.uniConnect.studentOrg.entity.StudentOrg;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SurveyRepository extends JpaRepository<Survey, Long> {

    List<Survey> findByStudentOrg(StudentOrg studentOrg);
}

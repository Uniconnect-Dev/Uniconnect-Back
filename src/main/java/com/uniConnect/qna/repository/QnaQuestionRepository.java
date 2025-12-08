package com.uniConnect.qna.repository;

import com.uniConnect.qna.entity.QnaQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Map;

public interface QnaQuestionRepository  extends JpaRepository<QnaQuestion, Long> {
    List<QnaQuestion> findAllByCompanyCompanyIdOrderByCreatedAtDesc(Long companyId);

    List<QnaQuestion> findAllByStudentOrgStudentOrgIdOrderByCreatedAtDesc(Long studentOrgId);
}
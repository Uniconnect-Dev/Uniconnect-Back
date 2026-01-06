package com.uniConnect.qna.repository;

import com.uniConnect.qna.entity.QnaQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import com.uniConnect.qna.enums.QuestionStatus;
import java.util.List;
import java.util.Map;

public interface QnaQuestionRepository  extends JpaRepository<QnaQuestion, Long> {
    List<QnaQuestion> findAllByCompanyCompanyIdOrderByCreatedAtDesc(Long companyId);

    List<QnaQuestion> findAllByStudentOrgStudentOrgIdOrderByCreatedAtDesc(Long studentOrgId);

    long countByCompanyCompanyId(Long companyId);
    long countByCompanyCompanyIdAndStatus(Long companyId, QuestionStatus status);

    long countByStudentOrgStudentOrgId(Long studentOrgId);
    long countByStudentOrgStudentOrgIdAndStatus(Long studentOrgId, QuestionStatus status);

    List<QnaQuestion> findAllByCompanyCompanyIdAndStatusOrderByCreatedAtDesc(
            Long companyId, QuestionStatus status
    );

    List<QnaQuestion> findAllByStudentOrgStudentOrgIdAndStatusOrderByCreatedAtDesc(
            Long studentOrgId, QuestionStatus status
    );
}
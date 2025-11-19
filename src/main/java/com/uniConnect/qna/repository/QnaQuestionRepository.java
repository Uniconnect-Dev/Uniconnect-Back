package com.uniConnect.qna.repository;

import com.uniConnect.qna.entity.QnaQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QnaQuestionRepository  extends JpaRepository<QnaQuestion, Long> {
}
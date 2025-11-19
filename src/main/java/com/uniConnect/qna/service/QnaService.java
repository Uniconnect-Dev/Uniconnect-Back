package com.uniConnect.qna.service;

import com.uniConnect.company.entity.Company;
import com.uniConnect.company.repository.CompanyRepository;
import com.uniConnect.studentOrg.entity.StudentOrg;
import com.uniConnect.studentOrg.repository.StudentOrgRepository;
import com.uniConnect.member.entity.User;
import com.uniConnect.member.repository.*;
import com.uniConnect.global.exception.CustomException;
import com.uniConnect.global.exception.ErrorCode;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import com.uniConnect.qna.dto.QnaCreateRequest;
import com.uniConnect.qna.dto.QuestionDetailResponse;
import com.uniConnect.qna.entity.QnaAnswer;
import com.uniConnect.qna.entity.QnaQuestion;
import com.uniConnect.qna.entity.QnaQuestionFile;
import com.uniConnect.qna.enums.QuestionStatus;
import com.uniConnect.qna.enums.QnaType;
import com.uniConnect.qna.repository.QnaAnswerRepository;
import com.uniConnect.qna.repository.QnaQuestionRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QnaService {

    private final CompanyRepository companyRepository;
    private final StudentOrgRepository studentOrgRepository;
    private final QnaQuestionRepository questionRepository;
    private final QnaAnswerRepository answerRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    // 문의 생성
    @Transactional
    public Long createQna(QnaCreateRequest req) {

        QnaQuestion.QnaQuestionBuilder builder = QnaQuestion.builder()
                .type(req.type())
                .title(req.title())
                .content(req.content())
                .password(passwordEncoder.encode(req.password()))
                .agreePersonalInfo(req.agreePersonalInfo())
                .agreeNotification(req.agreeNotification())
                .status(QuestionStatus.Pending);

        // 기업 문의
        if (req.type() == QnaType.Company) {
            Company company = companyRepository.findById(req.companyId())
                    .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));
            builder.company(company);
        }

        // 학생단체 문의
        if (req.type() == QnaType.StudentOrg) {
            StudentOrg org = studentOrgRepository.findById(req.studentOrgId())
                    .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));
            builder.studentOrg(org);
        }

        QnaQuestion question = builder.build();

        // 첨부파일 저장
        if (req.fileUrls() != null && !req.fileUrls().isEmpty()) {
            for (int i = 0; i < req.fileUrls().size(); i++) {
                QnaQuestionFile file = QnaQuestionFile.builder()
                        .question(question)
                        .fileUrl(req.fileUrls().get(i))
                        .originalFilename(req.originalNames().get(i))
                        .build();

                question.getFiles().add(file);
            }
        }

        return questionRepository.save(question).getQuestionId();
    }

    // 문의 상세 조회
    @Transactional(readOnly = true)
    public QuestionDetailResponse getQuestion(Long id) {

        QnaQuestion question = questionRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));

        List<String> fileUrls = question.getFiles().stream()
                .map(QnaQuestionFile::getFileUrl)
                .toList();

        QnaAnswer answer = question.getAnswer();

        return QuestionDetailResponse.builder()
                .questionId(id)
                .title(question.getTitle())
                .content(question.getContent())
                .status(question.getStatus().name())
                .files(fileUrls)
                .agreePersonalInfo(question.getAgreePersonalInfo())
                .agreeNotification(question.getAgreeNotification())
                .createdAt(question.getCreatedAt())
                .answerContent(answer != null ? answer.getContent() : null)
                .answerCreatedAt(answer != null ? answer.getCreatedAt() : null)
                .build();
    }

    // 관리자 답변 생성
    @Transactional
    public void createAnswer(Long questionId, Long userId, String content) {

        QnaQuestion question = questionRepository.findById(questionId)
                .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        QnaAnswer answer = QnaAnswer.builder()
                .question(question)
                .user(user)
                .content(content)
                .build();

        answerRepository.save(answer);

        question.setStatus(QuestionStatus.AnswerCompleted);
    }
}
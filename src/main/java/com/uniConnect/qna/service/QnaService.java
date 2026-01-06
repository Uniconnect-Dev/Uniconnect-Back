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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;

import com.uniConnect.s3.S3FileService;
import com.uniConnect.qna.dto.*;
import com.uniConnect.qna.entity.QnaAnswer;
import com.uniConnect.qna.entity.QnaQuestion;
import com.uniConnect.qna.entity.QnaQuestionFile;
import com.uniConnect.qna.enums.*;
import com.uniConnect.qna.repository.*;

import lombok.RequiredArgsConstructor;

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
    private final UserRepository userRepository;
    private final S3FileService s3FileService;

    @Value("${app.s3.bucket}")
    private String bucketName;

    // ----------------------------------------
    // 문의 생성
    // ----------------------------------------
    @Transactional
    public Long createQna(
            Long userId,
            String title,
            String content,
            List<MultipartFile> files,
            Boolean agreePersonalInfo,
            Boolean agreeNotification
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        QnaQuestion question = new QnaQuestion();
        question.setTitle(title);
        question.setContent(content);
        question.setAgreePersonalInfo(agreePersonalInfo);
        question.setAgreeNotification(agreeNotification);
        question.setStatus(QuestionStatus.Pending);

        if (user.getCompany() != null) {
            question.setType(QnaType.Company);
            question.setCompany(user.getCompany());
        } else if (user.getStudentOrg() != null) {
            question.setType(QnaType.StudentOrg);
            question.setStudentOrg(user.getStudentOrg());
        } else {
            throw new CustomException(ErrorCode.ENTITY_NOT_FOUND);
        }

        questionRepository.save(question);

        if (files != null && !files.isEmpty()) {
            uploadFiles(question, files);
        }

        return question.getQuestionId();
    }

    private void uploadFiles(QnaQuestion question, List<MultipartFile> files) {

        for (MultipartFile file : files) {

            try {
                String key = "qna/"
                        + question.getQuestionId()
                        + "_"
                        + file.getOriginalFilename();

                String uploadedKey = s3FileService.upload(bucketName, key, file);

                QnaQuestionFile qnaFile = QnaQuestionFile.builder()
                        .question(question)
                        .fileUrl(uploadedKey)
                        .originalFilename(file.getOriginalFilename())
                        .build();

                question.getFiles().add(qnaFile);

            } catch (Exception e) {
                throw new RuntimeException("Q&A 파일 업로드 실패", e);
            }
        }
    }


    // ----------------------------------------
    // 상세 조회
    // ----------------------------------------
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

    @Transactional(readOnly = true)
    public MyQnaSummaryResponse getMyQnaSummary(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        List<QnaQuestion> questions;
        long totalCount;
        long pendingCount;
        long answeredCount;

        if (user.getCompany() != null) {

            Long companyId = user.getCompany().getCompanyId();

            questions = questionRepository
                    .findAllByCompanyCompanyIdOrderByCreatedAtDesc(companyId);

            totalCount = questionRepository.countByCompanyCompanyId(companyId);
            pendingCount = questionRepository.countByCompanyCompanyIdAndStatus(
                    companyId, QuestionStatus.Pending
            );
            answeredCount = questionRepository.countByCompanyCompanyIdAndStatus(
                    companyId, QuestionStatus.AnswerCompleted
            );

        } else if (user.getStudentOrg() != null) {

            Long orgId = user.getStudentOrg().getStudentOrgId();

            questions = questionRepository
                    .findAllByStudentOrgStudentOrgIdOrderByCreatedAtDesc(orgId);

            totalCount = questionRepository.countByStudentOrgStudentOrgId(orgId);
            pendingCount = questionRepository.countByStudentOrgStudentOrgIdAndStatus(
                    orgId, QuestionStatus.Pending
            );
            answeredCount = questionRepository.countByStudentOrgStudentOrgIdAndStatus(
                    orgId, QuestionStatus.AnswerCompleted
            );

        } else {
            throw new CustomException(ErrorCode.ENTITY_NOT_FOUND);
        }

        return MyQnaSummaryResponse.builder()
                .totalCount(totalCount)
                .pendingCount(pendingCount)
                .answeredCount(answeredCount)
                .questions(
                        questions.stream()
                                .map(q -> QuestionDetailResponse.builder()
                                        .questionId(q.getQuestionId())
                                        .title(q.getTitle())
                                        .content(q.getContent())
                                        .status(q.getStatus().name())
                                        .createdAt(q.getCreatedAt())
                                        .answerContent(
                                                q.getAnswer() != null ? q.getAnswer().getContent() : null
                                        )
                                        .build()
                                )
                                .toList()
                )
                .build();
    }

    // ----------------------------------------
    // 관리자 답변
    // ----------------------------------------
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

    // ----------------------------------------
    // 기업 문의 목록 조회 (JWT 기반)
    // ----------------------------------------
    @Transactional(readOnly = true)
    public List<QuestionDetailResponse> getCompanyQnaListByUser(Long userId) {

        Company company = companyRepository.findByMainContactId(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));

        return questionRepository.findAllByCompanyCompanyIdOrderByCreatedAtDesc(company.getCompanyId())
                .stream()
                .map(q -> QuestionDetailResponse.builder()
                        .questionId(q.getQuestionId())
                        .title(q.getTitle())
                        .content(q.getContent())
                        .status(q.getStatus().name())
                        .createdAt(q.getCreatedAt())
                        .answerContent(q.getAnswer() != null ? q.getAnswer().getContent() : null)
                        .build())
                .toList();
    }

    // ----------------------------------------
    // 학생단체 문의 목록 조회 (JWT 기반)
    // ----------------------------------------
    @Transactional(readOnly = true)
    public List<QuestionDetailResponse> getStudentOrgQnaListByUser(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        StudentOrg org = user.getStudentOrg();
        if (org == null) {
            throw new CustomException(ErrorCode.ENTITY_NOT_FOUND);
        }

        Long orgId = org.getStudentOrgId();

        return questionRepository.findAllByStudentOrgStudentOrgIdOrderByCreatedAtDesc(orgId)
                .stream()
                .map(q -> QuestionDetailResponse.builder()
                        .questionId(q.getQuestionId())
                        .title(q.getTitle())
                        .content(q.getContent())
                        .status(q.getStatus().name())
                        .createdAt(q.getCreatedAt())
                        .answerContent(q.getAnswer() != null ? q.getAnswer().getContent() : null)
                        .build())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<QuestionDetailResponse> getMyQnaList(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        List<QnaQuestion> questions;

        if (user.getCompany() != null) {
            // 기업 사용자
            questions = questionRepository
                    .findAllByCompanyCompanyIdOrderByCreatedAtDesc(
                            user.getCompany().getCompanyId()
                    );

        } else if (user.getStudentOrg() != null) {
            // 학생단체 사용자
            questions = questionRepository
                    .findAllByStudentOrgStudentOrgIdOrderByCreatedAtDesc(
                            user.getStudentOrg().getStudentOrgId()
                    );

        } else {
            throw new CustomException(ErrorCode.ENTITY_NOT_FOUND);
        }

        return questions.stream()
                .map(q -> QuestionDetailResponse.builder()
                        .questionId(q.getQuestionId())
                        .title(q.getTitle())
                        .content(q.getContent())
                        .status(q.getStatus().name())
                        .createdAt(q.getCreatedAt())
                        .answerContent(
                                q.getAnswer() != null ? q.getAnswer().getContent() : null
                        )
                        .build())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<QuestionDetailResponse> getMyQnaByStatus(
            Long userId,
            QuestionStatus status
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        List<QnaQuestion> questions;

        if (user.getCompany() != null) {

            questions = questionRepository
                    .findAllByCompanyCompanyIdAndStatusOrderByCreatedAtDesc(
                            user.getCompany().getCompanyId(), status
                    );

        } else if (user.getStudentOrg() != null) {

            questions = questionRepository
                    .findAllByStudentOrgStudentOrgIdAndStatusOrderByCreatedAtDesc(
                            user.getStudentOrg().getStudentOrgId(), status
                    );

        } else {
            throw new CustomException(ErrorCode.ENTITY_NOT_FOUND);
        }

        return questions.stream()
                .map(q -> QuestionDetailResponse.builder()
                        .questionId(q.getQuestionId())
                        .title(q.getTitle())
                        .content(q.getContent())
                        .status(q.getStatus().name())
                        .createdAt(q.getCreatedAt())
                        .answerContent(
                                q.getAnswer() != null ? q.getAnswer().getContent() : null
                        )
                        .build())
                .toList();
    }

}

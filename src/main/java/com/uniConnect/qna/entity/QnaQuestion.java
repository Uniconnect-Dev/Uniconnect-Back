package com.uniConnect.qna.entity;

import com.uniConnect.company.entity.Company;
import com.uniConnect.studentOrg.entity.StudentOrg;
import com.uniConnect.common.entity.BaseEntity;
import com.uniConnect.qna.enums.QnaType;
import com.uniConnect.qna.enums.QuestionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "qna_questions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QnaQuestion extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long questionId;

    @Enumerated(EnumType.STRING)
    private QnaType type;  // COMPANY or STUDENT_ORG

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;  // 기업 문의일 경우

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_org_id")
    private StudentOrg studentOrg; // 학생단체 문의일 경우

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String password;

    private Boolean agreePersonalInfo;

    private Boolean agreeNotification;

    @Enumerated(EnumType.STRING)
    private QuestionStatus status;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QnaQuestionFile> files = new ArrayList<>();

    @OneToOne(mappedBy = "question", fetch = FetchType.LAZY)
    private QnaAnswer answer;
}

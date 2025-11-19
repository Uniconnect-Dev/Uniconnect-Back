package com.uniConnect.qna.entity;

import com.uniConnect.member.entity.User;
import com.uniConnect.qna.enums.QuestionStatus;
import com.uniConnect.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "qna_question_files")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QnaQuestionFile extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long fileId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id")
    private QnaQuestion question;

    private String fileUrl;

    private String originalFilename;
}

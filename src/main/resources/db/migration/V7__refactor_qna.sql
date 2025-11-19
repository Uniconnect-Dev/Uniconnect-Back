-- ================================
-- 1) 기존 Q&A 테이블 삭제
-- ================================

DROP TABLE IF EXISTS qna_answers CASCADE;
DROP TABLE IF EXISTS qna_questions CASCADE;

-- ================================
-- Q&A 질문 테이블 (기업 + 학생단체 통합)
-- ================================
CREATE TABLE qna_questions (
    question_id          BIGSERIAL PRIMARY KEY,
    type                 VARCHAR(20) NOT NULL,  -- COMPANY / STUDENT_ORG
    company_id           BIGINT,
    student_org_id       BIGINT,

    title                VARCHAR(200) NOT NULL,
    content              TEXT NOT NULL,
    password             VARCHAR(255) NOT NULL,

    agree_personal_info  BOOLEAN NOT NULL,
    agree_notification   BOOLEAN NOT NULL,

    status               VARCHAR(20) NOT NULL,  -- Pending / AnswerCompleted

    created_at           TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at           TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 회사 FK
ALTER TABLE qna_questions
ADD CONSTRAINT fk_qna_company
FOREIGN KEY (company_id)
REFERENCES companies(company_id)
ON DELETE SET NULL;

-- 학생단체 FK
ALTER TABLE qna_questions
ADD CONSTRAINT fk_qna_student_org
FOREIGN KEY (student_org_id)
REFERENCES student_orgs(student_org_id)
ON DELETE SET NULL;


-- ================================
-- Q&A 파일 테이블
-- ================================
CREATE TABLE qna_question_files (
    file_id             BIGSERIAL PRIMARY KEY,
    question_id         BIGINT NOT NULL,
    file_url            TEXT NOT NULL,
    original_filename   VARCHAR(255),

    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE qna_question_files
ADD CONSTRAINT fk_qna_question_file
FOREIGN KEY (question_id)
REFERENCES qna_questions(question_id)
ON DELETE CASCADE;


-- ================================
-- Q&A 답변 테이블 (관리자)
-- ================================
CREATE TABLE qna_answers (
    answer_id         BIGSERIAL PRIMARY KEY,
    question_id       BIGINT NOT NULL,
    admin_user_id     BIGINT NOT NULL,   -- 관리자 user_id
    content           TEXT NOT NULL,
    created_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE qna_answers
ADD CONSTRAINT fk_qna_answer_question
FOREIGN KEY (question_id)
REFERENCES qna_questions(question_id)
ON DELETE CASCADE;

ALTER TABLE qna_answers
ADD CONSTRAINT fk_qna_answer_admin
FOREIGN KEY (admin_user_id)
REFERENCES users(user_id)
ON DELETE CASCADE;

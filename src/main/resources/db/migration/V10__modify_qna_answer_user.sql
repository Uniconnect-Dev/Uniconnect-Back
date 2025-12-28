-- ========================================
-- QnA Answer 엔티티에서 admin_user_id 삭제
-- user_id 추가
-- ========================================

-- 1) 기존 FK 및 컬럼 삭제
ALTER TABLE qna_answers
DROP CONSTRAINT IF EXISTS fk_qna_answer_admin_user;

ALTER TABLE qna_answers
DROP COLUMN IF EXISTS admin_user_id;

-- 2) user_id 컬럼 추가 (nullable)
ALTER TABLE qna_answers
ADD COLUMN user_id BIGINT;

-- 3) FK 설정 (users 테이블)
ALTER TABLE qna_answers
ADD CONSTRAINT fk_qna_answer_user
FOREIGN KEY (user_id) REFERENCES users(user_id);

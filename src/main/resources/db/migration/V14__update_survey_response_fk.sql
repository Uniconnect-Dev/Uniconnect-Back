-- 기존 FK 삭제
ALTER TABLE survey_responses
DROP CONSTRAINT IF EXISTS fk_surveyresponse_survey;

-- SurveyResponse.survey_id → survey.survey_id FK 재설정
ALTER TABLE survey_responses
ADD CONSTRAINT fk_surveyresponse_survey
FOREIGN KEY (survey_id)
REFERENCES survey (survey_id)
ON DELETE CASCADE;

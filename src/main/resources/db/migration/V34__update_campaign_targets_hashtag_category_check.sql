-- 1. 기존 CHECK 제약조건 제거
ALTER TABLE campaign_targets
DROP CONSTRAINT IF EXISTS campaign_targets_hashtag_category_check;

-- 2. PascalCase enum 기준으로 CHECK 제약조건 재생성
ALTER TABLE campaign_targets
ADD CONSTRAINT campaign_targets_hashtag_category_check
CHECK (
    hashtag_category IN (
        'Age',
        'Major',
        'StudentType',
        'Region',
        'Hobby',
        'Interest',
        'Lifestyle'
    )
);

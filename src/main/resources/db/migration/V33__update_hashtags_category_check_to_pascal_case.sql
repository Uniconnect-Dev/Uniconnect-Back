-- 1. 기존 category CHECK 제약조건 제거
ALTER TABLE hashtags
DROP CONSTRAINT IF EXISTS hashtags_category_check;

-- 2. PascalCase enum 값 기준으로 새로운 CHECK 제약조건 추가
ALTER TABLE hashtags
ADD CONSTRAINT hashtags_category_check
CHECK (
    category IN (
        'Age',
        'Major',
        'StudentType',
        'Region',
        'Hobby',
        'Interest',
        'Lifestyle'
    )
);

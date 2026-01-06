-- 1. sampling_target_keyword.category 제약조건 수정
ALTER TABLE sampling_target_keyword
DROP CONSTRAINT IF EXISTS sampling_target_keyword_category_check;

ALTER TABLE sampling_target_keyword
ADD CONSTRAINT sampling_target_keyword_category_check
CHECK (
    category IN (
        'BasicInfo',
        'Lifestyle',
        'EventNature',
        'Etc'
    )
);

-- 2. sampling_target_selection.category 제약조건 수정
ALTER TABLE sampling_target_selection
DROP CONSTRAINT IF EXISTS sampling_target_selection_category_check;

ALTER TABLE sampling_target_selection
ADD CONSTRAINT sampling_target_selection_category_check
CHECK (
    category IN (
        'BasicInfo',
        'Lifestyle',
        'EventNature',
        'Etc'
    )
);

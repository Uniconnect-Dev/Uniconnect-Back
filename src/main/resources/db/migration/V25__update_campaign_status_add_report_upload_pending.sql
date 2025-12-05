-- 기존 CHECK 제약조건 삭제
ALTER TABLE public.campaigns
    DROP CONSTRAINT IF EXISTS campaigns_status_check;

-- 새로운 CHECK 제약조건 추가 (ReportUploadPending 포함)
ALTER TABLE public.campaigns
    ADD CONSTRAINT campaigns_status_check
    CHECK (
        status IN (
            'InTransit',
            'OnHold',
            'Processing',
            'Rejecting',
            'Completed',
            'ReportUploadPending'
        )
    );
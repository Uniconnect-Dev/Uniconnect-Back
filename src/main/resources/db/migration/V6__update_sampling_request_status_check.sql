ALTER TABLE sampling_request
    DROP CONSTRAINT sampling_request_status_check;

ALTER TABLE sampling_request
    ADD CONSTRAINT sampling_request_status_check
    CHECK (status IN (
        'Draft',
        'Submitted',
        'MatchingRequested',  -- ★ 추가된 상태
        'Approved',
        'Rejected',
        'ContractPending',
        'ContractApprovalPending',
        'ReceiptPending',
        'ReceiptApprovalPending',
        'ReportPending',
        'ReportApprovalPending',
        'SurveyPending',
        'Completed'
    ));
CREATE TABLE sampling_selected_company (
    id BIGSERIAL PRIMARY KEY,
    sampling_request_id BIGINT NOT NULL,
    company_id BIGINT NOT NULL,

    CONSTRAINT fk_selected_company_request
        FOREIGN KEY (sampling_request_id)
        REFERENCES sampling_request (sampling_request_id)
        ON DELETE CASCADE,

    CONSTRAINT fk_selected_company_company
        FOREIGN KEY (company_id)
        REFERENCES companies (company_id)
        ON DELETE CASCADE
);

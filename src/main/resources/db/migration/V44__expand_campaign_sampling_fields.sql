ALTER TABLE campaigns
ADD COLUMN preferred_industry_1 VARCHAR(100),
ADD COLUMN preferred_industry_2 VARCHAR(100),

ADD COLUMN recommended_sampling_qty INT,
ADD COLUMN booth_fee INT,

ADD COLUMN extra_request TEXT,
ADD COLUMN proposal_file_url TEXT;
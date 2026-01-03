ALTER TABLE campaigns
ADD COLUMN expected_participants INT,
ADD COLUMN expected_exposures INT,
ADD COLUMN target_age_desc VARCHAR(100),
ADD COLUMN target_major_desc VARCHAR(100);
ALTER TABLE companies
ADD COLUMN sampling_purpose TEXT,
ADD COLUMN sampling_start_date DATE,
ADD COLUMN sampling_end_date DATE,
ADD COLUMN product_name VARCHAR(100),
ADD COLUMN product_count INT;
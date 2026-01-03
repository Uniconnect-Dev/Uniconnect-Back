ALTER TABLE campaigns
ADD COLUMN collaboration_type VARCHAR(30);

ALTER TABLE campaigns
ADD COLUMN event_programs JSONB;

ALTER TABLE campaigns
ADD COLUMN promotion_plans JSONB;

ALTER TABLE campaigns
ADD COLUMN marketing_methods JSONB;

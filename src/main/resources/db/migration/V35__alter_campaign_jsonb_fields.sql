ALTER TABLE campaigns
ALTER COLUMN event_programs
TYPE jsonb
USING event_programs::jsonb;

ALTER TABLE campaigns
ALTER COLUMN promotion_plans
TYPE jsonb
USING promotion_plans::jsonb;

ALTER TABLE campaigns
ALTER COLUMN marketing_methods
TYPE jsonb
USING marketing_methods::jsonb;


ALTER TABLE collaboration_proposals
ALTER COLUMN collaboration_methods_json TYPE jsonb
USING collaboration_methods_json::jsonb;

ALTER TABLE collaboration_proposals
ALTER COLUMN expected_outcomes_json TYPE jsonb
USING expected_outcomes_json::jsonb;

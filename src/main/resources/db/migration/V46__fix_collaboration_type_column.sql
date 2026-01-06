ALTER TABLE collaboration_match_requests
ALTER COLUMN collaboration_type TYPE VARCHAR(30);

ALTER TABLE collaboration_match_requests
ALTER COLUMN collaboration_type SET NOT NULL;

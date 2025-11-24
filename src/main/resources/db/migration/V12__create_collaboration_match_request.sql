ALTER TABLE collaboration_match_requests
RENAME COLUMN matching_request_id TO id;

ALTER TABLE collaboration_match_requests
ALTER COLUMN id SET DATA TYPE BIGINT;
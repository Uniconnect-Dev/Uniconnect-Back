UPDATE collaboration_match_requests
SET industry = 'Fnb'
WHERE industry NOT IN ('Fnb', 'Beauty', 'Education', 'OnlineService', 'Stationery', 'It', 'Travel');

ALTER TABLE collaboration_match_requests
  ADD CONSTRAINT chk_industry_valid
  CHECK (industry IN (
        'Fnb',
        'Beauty',
        'Education',
        'OnlineService',
        'Stationery',
        'It',
        'Travel'
  ));

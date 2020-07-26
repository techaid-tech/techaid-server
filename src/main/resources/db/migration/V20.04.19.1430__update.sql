ALTER TABLE volunteers RENAME COLUMN ward TO post_code;
ALTER TABLE volunteers ADD COLUMN coordinates jsonb;
ALTER TABLE kits
  ADD COLUMN volunteer_id int8,
  ADD COLUMN coordinates jsonb;
ALTER TABLE donors
  ADD COLUMN coordinates jsonb;
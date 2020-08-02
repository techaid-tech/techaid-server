alter table if exists kits
  alter column archived TYPE varchar(1) USING CASE archived WHEN true THEN 'Y' ELSE 'N' end;
alter table if exists organisations
  alter column archived TYPE varchar(1) USING CASE archived WHEN true THEN 'Y' ELSE 'N' end;
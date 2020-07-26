alter table if exists organisations add column contact varchar(255);
alter table if exists posts add column secured boolean not null default false;
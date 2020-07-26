create sequence email_template_sequence start 1 increment 1;
create sequence organisation_sequence start 1 increment 1;
alter table if exists kits add column organisation_id int8;

create table email_templates (
  id int8 not null,
  created_at timestamp,
  updated_at timestamp,
  active boolean not null default false,
  body text,
  subject varchar(255),
  primary key (id)
);

create table organisations (
  id int8 not null,
  attributes jsonb,
  created_at timestamp,
  email varchar(255),
  name varchar(255),
  phone_number varchar(255),
  updated_at timestamp,
  website varchar(255),
  primary key (id)
);

alter table if exists kits
   add constraint FKlih5h0mbnb9jx5pqj4doyc19
   foreign key (organisation_id)
   references organisations;


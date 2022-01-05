create sequence kits_audit_sequence start 1 increment 1;

create table kits_audit (
    audit_id int8 not null default nextval('kits_audit_sequence'),
    id int8 not null,
    age int4 not null,
    donor_id int8,
    created_at timestamp without time zone,
    location varchar(255),
    model varchar(255),
    status varchar(255),
    type varchar(255),
    updated_at timestamp without time zone,
    attributes jsonb,
    coordinates jsonb,
    archived varchar(1) default false,
    organisation_id int8,
    primary key (audit_id)
);

create or replace function kitauditfunc() returns trigger as $example_table$
   begin
      insert into kits_audit(id, age, donor_id, created_at, location, model, status, type, updated_at, attributes, coordinates, archived, organisation_id)
      values (old.id, old.age, old.donor_id, old.created_at, old.location, old.model, old.status, old.type, old.updated_at, old.attributes, old.coordinates, old.archived, old.organisation_id);
      return new;
   end;
$example_table$ language plpgsql;

create trigger kit_audit_trigger after update on kits for each row execute procedure kitauditfunc();


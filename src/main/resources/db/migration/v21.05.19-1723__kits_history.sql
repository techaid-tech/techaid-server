create sequence kits_history_sequence start 1 increment 1;

create table kits_history (
    entry_id int8 not null default nextval('kits_history_sequence'),
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
    primary key (entry_id)
);

create function insert_kit_history_entry() returns trigger as $table$
   begin
      insert into kits_history(id, age, donor_id, created_at, location, model, status, type, updated_at, attributes, coordinates, archived, organisation_id)
      values (old.id, old.age, old.donor_id, old.created_at, old.location, old.model, old.status, old.type, old.updated_at, old.attributes, old.coordinates, old.archived, old.organisation_id);
      return new;
   end;
$table$ language plpgsql;

create trigger kits_history_trigger after update on kits for each row execute procedure insert_kit_history_entry();


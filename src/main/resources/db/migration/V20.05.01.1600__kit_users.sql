alter table kits
  drop column volunteer_id,
  add column archived boolean default false;

create table kit_volunteers (
    created_at timestamp,
    type varchar(255),
    kit_id int8 not null,
    volunteer_id int8 not null,
    primary key (kit_id, type, volunteer_id)
);

alter table if exists kit_volunteers
   add constraint FK279bv2icnyu7lik3f4m4cyjv0
   foreign key (kit_id)
   references kits;

alter table if exists kit_volunteers
   add constraint FKa45nb0ccimms00ruh56bi8a31
   foreign key (volunteer_id)
   references volunteers;

alter table if exists kits
   add constraint FKtx5w8x58hp99pssrocdy5d4o
   foreign key (donor_id)
   references donors;




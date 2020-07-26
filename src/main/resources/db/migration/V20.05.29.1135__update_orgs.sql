alter table if exists organisations
  add column volunteer_id int8,
  add column archived boolean default false;

alter table if exists organisations
   add constraint FKgk1igqvcvj38qbu8tr81196mi
   foreign key (volunteer_id)
   references volunteers;

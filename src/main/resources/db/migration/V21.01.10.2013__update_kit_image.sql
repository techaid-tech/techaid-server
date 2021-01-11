create table if not exists kit_images (
	kit_id integer,
	images jsonb,
	PRIMARY KEY(kit_id),
    CONSTRAINT fk_kit FOREIGN KEY(kit_id) REFERENCES kits(id)
);

insert into kit_images(kit_id, images)
select id, attributes->'images' from kits where jsonb_array_length(attributes->'images') > 0;

update kits set attributes = attributes - 'images';
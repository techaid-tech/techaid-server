create sequence donor_sequence start 1 increment 1;
create sequence kit_sequence start 1 increment 1;
create sequence volunteer_sequence start 1 increment 1;

    create table donors (
       id int8 not null,
        created_at timestamp,
        email varchar(255),
        name varchar(255),
        phone_number varchar(255),
        post_code varchar(255),
        referral varchar(255),
        updated_at timestamp,
        primary key (id)
    );

    create table kits (
       id int8 not null,
        age int4 not null,
        donor_id int8,
        created_at timestamp,
        location varchar(255),
        model varchar(255),
        status int4,
        type int4,
        updated_at timestamp,
        attributes jsonb,
        primary key (id)
    );

    create table volunteers (
       id int8 not null,
        availability varchar(255),
        created_at timestamp,
        email varchar(255),
        expertise varchar(255),
        name varchar(255),
        phone_number varchar(255),
        storage varchar(255),
        sub_group varchar(255),
        transport varchar(255),
        updated_at timestamp,
        ward varchar(255),
        consent varchar(255),
        primary key (id)
    );

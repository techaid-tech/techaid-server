create sequence faq_sequence start 1 increment 1;
create sequence post_sequence start 1 increment 1;
create table faqs (
    id int8 not null,
    created_at timestamp,
    title varchar(255),
    content TEXT,
    published boolean,
    updated_at timestamp,
    position int not null,
    primary key (id)
);

create table posts (
    id int8 not null,
    created_at timestamp,
    slug varchar(255) UNIQUE,
    title varchar(255),
    content TEXT,
    published boolean,
    updated_at timestamp,
    primary key (id)
);
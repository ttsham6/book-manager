create table authors (
    id bigserial primary key,
    name varchar(255) not null,
    birth_date date not null,
    created_at timestamptz not null default current_timestamp,
    updated_at timestamptz not null default current_timestamp
);

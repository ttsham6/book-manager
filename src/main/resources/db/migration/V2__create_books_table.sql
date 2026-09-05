create table books (
    id bigserial primary key,
    title varchar(255) not null,
    price bigint not null check (price >= 0),
    publication_status varchar(32) not null check (publication_status in ('UNPUBLISHED', 'PUBLISHED')),
    created_at timestamptz not null default current_timestamp,
    updated_at timestamptz not null default current_timestamp
);

create table book_authors (
    book_id bigint not null references books(id) on delete cascade,
    author_id bigint not null references authors(id),
    primary key (book_id, author_id)
);

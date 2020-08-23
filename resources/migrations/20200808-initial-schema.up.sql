CREATE TABLE IF NOT EXISTS flashcards (
       id uuid primary key,
       created_at timestamp not null default now(),
       updated_at timestamp not null default now(),
       question varchar(512) not null unique,
       answer varchar(128) not null
);

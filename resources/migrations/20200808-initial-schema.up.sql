CREATE TABLE IF NOT EXISTS flashcards (
       id integer primary key,
       created_at text not null,
       updated_at text not null,
       question text not null unique,
       answer text not null
);
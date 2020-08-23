CREATE TABLE IF NOT EXISTS decks (
       id uuid primary key not null unique,
       created_at timestamp not null,
       updated_at timestamp not null,
       label varchar(128) not null unique,
       description text
);
--;;
ALTER TABLE flashcards ADD COLUMN deck_id uuid REFERENCES decks (id);

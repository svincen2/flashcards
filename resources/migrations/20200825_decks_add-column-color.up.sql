-- decks: add column color
ALTER TABLE decks ADD COLUMN color varchar(6) NOT NULL DEFAULT 'ffffff';

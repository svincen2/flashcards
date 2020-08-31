-- cards: drop column deck_id
ALTER TABLE cards ADD COLUMN deck_id uuid REFERENCES decks (id);
--;;

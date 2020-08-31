-- deck_cards: add table deck_cards
CREATE TABLE IF NOT EXISTS deck_cards (
       id uuid NOT NULL PRIMARY KEY,
       created_at timestamp NOT NULL DEFAULT now(),
       updated_at timestamp NOT NULL DEFAULT now(),
       deck_id uuid NOT NULL REFERENCES decks (id),
       card_id uuid NOT NULL REFERENCES cards (id),
       UNIQUE (deck_id, card_id)
);
--;;

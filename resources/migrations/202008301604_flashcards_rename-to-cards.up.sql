-- rename indices
ALTER INDEX flashcards_pkey RENAME TO cards_pkey;
--;;

--
ALTER INDEX flashcards_question_key RENAME TO cards_question_key;
--;;

-- rename constraints
ALTER TABLE flashcards RENAME CONSTRAINT flashcards_deck_id_fkey TO cards_deck_id_fkey;
--;;

-- flashcards: rename to cards
ALTER TABLE flashcards RENAME TO cards;
--;;

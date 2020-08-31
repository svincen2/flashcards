-- rename indices
ALTER INDEX cards_pkey RENAME TO flashcards_pkey;
--;;

--
ALTER INDEX cards_question_key RENAME TO flashcards_question_key;
--;;

-- rename constraints
ALTER TABLE cards RENAME CONSTRAINT cards_deck_id_fkey TO flashcards_deck_id_fkey;
--;;

-- flashcards: rename to cards
ALTER TABLE cards RENAME TO flashcards;
--;;

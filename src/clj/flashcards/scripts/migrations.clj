(ns flashcards.scripts.migrations
  (:require [flashcards.db :as db]
            [flashcards.models.cards :as c]
            [flashcards.models.deck-cards :as dc]))

(defn insert-deck-cards
  []
  (let [cards (c/fetch)]
    (doseq [card cards]
      (dc/insert! (:deck-id card) (:id card)))))

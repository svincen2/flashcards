(ns flashcards.models.cards
  (:require [flashcards.db :as db]
            [flashcards.models.deck-cards :as dc]
            [taoensso.timbre :as log]
            [honeysql.core :as sql]))

(def schema {:table :cards
             :alias :c
             :columns [:id
                       :created-at
                       :updated-at
                       :question
                       :answer]})

(defn fetch
  []
  (db/query {:select (db/columns schema)
             :from [(db/table schema)]
             :order-by [[(db/column schema :created-at) :desc]]}))

(defn fetch-with-deck-ids
  []
  (let [cards (db/query {:select (conj (db/aliased-columns schema)
                                       (sql/raw "array_agg(dc.deck_id) as deck_ids"))
                         :from [(db/aliased-table schema)]
                         :left-join [[:deck-cards :dc] [:= :c.id :dc.card-id]]
                         :group-by [:c.id]
                         :order-by [[(db/aliased-column schema :created-at) :desc]]})]
    (map (fn [card]
           (update card :deck-ids #(into [] (.getArray %))))
         cards)))

(defn create!
  [question answer deck-ids]
  (let [card (db/insert! (:table schema)
                         {:question question
                          :answer answer})]
    (when (seq deck-ids)
      (doall (map #(dc/insert! % (:id card)) deck-ids)))
    card))

(defn delete!
  [id]
  (dc/delete-by-card-id! id)
  (db/delete! (:table schema)
              [:= :id id]))

(comment
  (db/columns schema)
  (db/table schema)
  (db/aliased-columns schema)
  (db/aliased-table schema)
  (fetch)

  (create! "Hello" "Goodbye" nil)

  (fetch-with-deck-ids)

  (delete! 1)
  )

(ns flashcards.models.deck-cards
  (:require [flashcards.db :as db]))

(def ^:private schema {:table :deck-cards
                       :columns [:id
                                 :created-at
                                 :updated-at
                                 :deck-id
                                 :card-id]})

(defn insert!
  [deck-id card-id]
  (db/insert! (:table schema)
              {:deck-id deck-id
               :card-id card-id}))

(defn delete-by-card-id!
  [card-id]
  (db/delete! (:table schema)
              [:= :card-id card-id]))


(comment

  (insert! #uuid "cdf89a23-f3f7-4fe6-b60a-5eada7e63d48" #uuid "5d447d48-b404-4fed-967f-586b49eb5606")
  )

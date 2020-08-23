(ns flashcards.models.decks
  (:require [flashcards.db :as db]
            [taoensso.timbre :as log]))

(def schema {:table :decks
             :columns [:id
                       :created-at
                       :updated-at
                       :label
                       :description]})

(defn fetch
  []
  (db/query {:select (:columns schema)
             :from [(:table schema)]
             :order-by [[:created-at :desc]]}))

(defn create!
  [label description]
  (db/insert! (:table schema)
              {:label label
               :description description}))

(defn delete-by-id!
  [id]
  (db/delete! (:table schema)
              [:= :id id]))

(defn delete-by-label!
  [label]
  (db/delete! (:table schema)
              [:= :label label]))

(comment

  (fetch)

  (create! "AP Bio" "AP Bio Fall 2020")

  (delete-by-label! "AP Bio")
  (delete-by-id! #uuid "9ecae69b-a2f7-4df7-b1f5-cf2c487a81f3")
  )

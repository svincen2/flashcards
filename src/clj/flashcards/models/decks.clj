(ns flashcards.models.decks
  (:require [flashcards.db :as db]
            [taoensso.timbre :as log]))

(def schema {:table :decks
             :alias :d
             :columns [:id
                       :created-at
                       :updated-at
                       :label
                       :color]})

(defn fetch
  []
  (db/query {:select (db/columns schema)
             :from [(db/table schema)]
             :order-by [[:created-at :desc]]}))

(defn create!
  [label color]
  (db/insert! (:table schema)
              {:label label
               :color color}))

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

  (create! "AP Bio")

  (delete-by-label! "AP Bio")
  (delete-by-id! #uuid "9ecae69b-a2f7-4df7-b1f5-cf2c487a81f3")
  )

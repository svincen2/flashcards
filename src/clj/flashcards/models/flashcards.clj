(ns flashcards.models.flashcards
  (:require [flashcards.db :as db]
            [taoensso.timbre :as log]))

(def schema {:table :flashcards
             :columns [:id
                       :created-at
                       :updated-at
                       :question
                       :answer]})

(defn fetch
  []
  (db/query {:select (:columns schema)
             :from [(:table schema)]
             :order-by [[:created-at :desc]]}))

(defn create!
  [question answer]
  (db/insert! (:table schema)
              {:question question
               :answer answer}))

(defn delete!
  [id]
  (db/delete! (:table schema)
              [:= :id id]))

(comment

  (fetch)

  (create! "Hello" "Goodbye")

  (delete! 1)
  )

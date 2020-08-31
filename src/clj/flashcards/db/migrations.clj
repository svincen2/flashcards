(ns flashcards.db.migrations
  (:require [flashcards.db :as db]
            [ragtime.jdbc :as jdbc]
            [ragtime.repl :as repl]
            [clojure.string :as string])
  (:import [java.time LocalDateTime]
           [java.time.format DateTimeFormatter]))

(defn config
  []
  {:datastore (jdbc/sql-database db/spec)
   :migrations (jdbc/load-resources "migrations")})

(defn migrate
  []
  (repl/migrate (config)))

(defn rollback
  []
  (repl/rollback (config)))

(defn create-migration
  [table description]
  (let [format (DateTimeFormatter/ofPattern "yyyyMMddHHmm")
        today (.format (LocalDateTime/now) format)
        name (string/split description #"\s+")
        [up down] (map (fn [type]
                         (str today \_ table \_ (string/join \- name) \. type ".sql"))
                       ["up" "down"])]
    (spit (str "resources/migrations/" up) (str "-- " table ": " description))
    (spit (str "resources/migrations/" down) (str "-- " table ": " description))))

(comment




  (create-migration "cards" "drop column deck_id")

  (migrate)

  (rollback)


  )

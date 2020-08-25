(ns flashcards.db.migrations
  (:require [flashcards.db :as db]
            [ragtime.jdbc :as jdbc]
            [ragtime.repl :as repl]
            [clojure.java.io :as io]
            [clojure.string :as string])
  (:import [java.time LocalDate]
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
  (let [today (.format (LocalDate/now) DateTimeFormatter/BASIC_ISO_DATE)
        name (string/split description #"\s+")
        [up down] (map (fn [type]
                         (str today \_ table \_ (string/join \- name) \. type ".sql"))
                       ["up" "down"])]
    (spit (str "resources/migrations/" up) (str "-- " table ": " description))
    (spit (str "resources/migrations/" down) (str "-- " table ": " description))))

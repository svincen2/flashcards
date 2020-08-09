(ns flashcards.db.migrations
  (:require [flashcards.db :as db]
            [ragtime.jdbc :as jdbc]
            [ragtime.repl :as repl]))

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

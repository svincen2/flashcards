(ns flashcards.db
  (:require [clojure.java.jdbc :as jdbc]
            [honeysql.core :as sql]
            [clojure.string :as str]
            [taoensso.timbre :as log])
  (:import [java.sql Timestamp]
           [java.time Instant]
           [java.util Date UUID]))

#_(def spec {:dbtype "sqlite"
             :dbname "db.sqlite"})

(def spec {:dbtype "postgresql"
           :dbname "flashcards"
           :host "localhost"})

(defn ^:private clj->sql
  [x]
  (-> x
      (name)
      (str/replace #"-" "_")))

(defn ^:private sql->clj
  [x]
  (-> x
      (str/lower-case)
      (str/replace #"_" "-")
      (keyword)))

(defn ^:private convert-instants
  [row]
  (into {} (map (fn [[k v]]
                  {k (if (instance? Date v)
                       (.toInstant v)
                       v)})
                row)))

(defn ^:private prepare-insert
  [row]
  (let [created-at (Timestamp/from (Instant/now))]
    (merge {:id (UUID/randomUUID)
            :created-at created-at
            :updated-at created-at}
           (into {} (map (fn [[k v]]
                           {k (if (instance? Instant v)
                                (Timestamp/from v)
                                v)})
                         row)))))

(defn query
  [sqlmap]
  (let [sql (sql/format sqlmap)]
    (log/info "query" sql)
    (->> (jdbc/query spec sql
                     {:identifiers sql->clj
                      :entities clj->sql})
         (map convert-instants))))

(defn fetch!
  [table id]
  (log/info "fetch!" table id)
  (-> (query {:select [:*]
              :from [table]
              :where [:= :id id]})
      (first)
      (convert-instants)))

(defn insert!
  [table row]
  (log/info "insert!" table row)
  (-> (jdbc/insert! spec table
                    (prepare-insert row)
                    {:identifiers sql->clj
                     :entities clj->sql})
      (first)
      (convert-instants)))

(defn delete!
  [table where]
  (log/info "delete!" table where)
  (let [sql (sql/format {:delete-from table
                         :where where})]
    (jdbc/execute! spec sql
                   {:identifiers sql->clj
                    :entities clj->sql})))

(comment

  (query {:select [:*] :from [:flashcards]})

  (delete! :flashcards [:= :question "Hello?"])

  (insert! :flashcards {:question "Hello?"
                        :answer "Goodbye"
                        :created-at (Instant/now)}))

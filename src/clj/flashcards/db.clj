(ns flashcards.db
  (:require [clojure.java.jdbc :as jdbc]
            [honeysql.core :as sql]
            [clojure.string :as str]
            [taoensso.timbre :as log])
  (:import [java.time Instant]))

(def spec {:dbtype "sqlite"
           :dbname "db.sqlite"})

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

(defn ^:private parse-instants
  [row]
  (let [instant-cols [:created-at :updated-at]]
    (reduce (fn [row col]
              (update row col #(Instant/parse %)))
            row
            instant-cols)))

(defn query
  [sqlmap]
  (let [sql (sql/format sqlmap)]
    (log/info "query" sql)
    (->> (jdbc/query spec sql
                 {:identifiers sql->clj
                  :entities clj->sql})
         #_(map parse-instants))))

(defn fetch!
  [table id]
  (log/info "fetch!" table id)
  (first (query {:select [:*]
                 :from [table]
                 :where [:= :id id]})))

(defn insert!
  [table row]
  (log/info "insert!" table row)
  (let [created-at (or (:created-at row) (Instant/now))
        row (merge row {:created-at created-at
                        :updated-at created-at})
        id (-> (jdbc/insert! spec table
                             row
                             {:identifiers sql->clj
                              :entities clj->sql})
               (first)
               (get (keyword "last-insert-rowid()")))]
    (fetch! table id)))

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

  (insert! :flashcards {:question "Hello?"
                        :answer "Goodbye"
                        :created-at (Instant/now)})

  )

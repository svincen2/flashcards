(ns flashcards.handlers.api
  (:require [flashcards.server.response :as r]
            [flashcards.models.flashcards :as f]
            [taoensso.timbre :as log]))

(defn access-control
  [resource {{origin "origin"} :headers}]
  (let [allowed-origins #{"http://localhost:8280"
                          "http://localhost:3000"}
        access {:flashcards {:methods ["GET" "POST" "DELETE"]
                             :headers ["Content-Type"]}}]
    (if (contains? allowed-origins origin)
      (r/access-control origin (get access resource))
      (r/forbidden origin))))

(defn ping
  []
  (r/ok {:message "pong"}))

(defn echo
  [{:keys [edn-body]}]
  (r/ok edn-body))

(defn get-flashcards
  []
  (-> (r/ok (f/fetch))))

(defn create-flashcard
  [{{:keys [question answer]} :edn-body}]
  (r/ok (f/create! question answer)))

(defn delete-flashcard
  [{{:keys [id]} :edn-body}]
  (r/ok (f/delete! id)))

(comment
  (log/info "keep me in the ns")
  )

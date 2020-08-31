(ns flashcards.handlers.api
  (:require [flashcards.server.response :as r]
            [flashcards.models.cards :as c]
            [flashcards.models.decks :as d]
            [taoensso.timbre :as log]))

(defn access-control
  [resource {{origin "origin"} :headers}]
  (let [allowed-origins #{"http://localhost:8280"
                          "http://localhost:3000"}
        access {:cards {:methods ["GET" "POST" "DELETE"]
                        :headers ["Content-Type"]}
                :decks {:methods ["GET" "POST" "DELETE"]
                        :headers ["Content-Type"]}}]
    (if (contains? allowed-origins origin)
      (r/access-control origin (get access resource))
      (r/forbidden origin))))

(defn ping
  []
  (r/ok {:message "pong"}))

(defn get-cards
  []
  (-> (r/ok (c/fetch-with-deck-ids))))

(defn create-card
  [{{:keys [question answer deck-ids]} :edn-body}]
  (r/ok (c/create! question answer deck-ids)))

(defn delete-card
  [{{:keys [id]} :edn-body}]
  (r/ok (c/delete! id)))

(defn get-decks
  []
  (-> (r/ok (d/fetch))))

(defn create-deck
  [{{:keys [label color]} :edn-body}]
  (r/ok (d/create! label color)))

(defn delete-deck
  [{{:keys [id]} :edn-body}]
  (r/ok (d/delete-by-id! id)))

(comment
  (log/info "keep me in the ns")
  )

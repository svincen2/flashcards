(ns flashcards.handler
  (:require [compojure.core :as c]
            [compojure.route :as cr]
            [shadow.http.push-state :as push-state]
            [flashcards.handlers.api :as api]
            [flashcards.server.response :as r]
            [flashcards.server.middleware :as m]
            [taoensso.timbre :as log]))

(c/defroutes routes
  ;; App (UI)
  (c/GET "/" [] (r/resource "index.html" {:root "public"}))
  (cr/resources "/")
  ;; API (Backend)
  (c/context
    "/api" []
    (c/GET "/ping" [] (api/ping))
    (c/POST "/echo" req (api/echo req))
    (c/context
     "/flashcards" []
     (c/OPTIONS "/" req (api/access-control :flashcards req))
     (c/GET "/" [] (api/get-flashcards))
     (c/POST "/" req (api/create-flashcard req))
     (c/DELETE "/" req (api/delete-flashcard req))))
  ;; Anything else
  (c/ANY "*" [] (r/not-found nil)))

(def dev-handler (-> #'routes
                     m/wrap-reload
                     push-state/handle))

(def handler (-> routes
                 m/wrap-parse-edn-body))

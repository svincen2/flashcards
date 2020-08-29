(ns flashcards.handler
  (:require [compojure.core :as c]
            [compojure.route :as cr]
            [flashcards.handlers.api :as api]
            [flashcards.routes :as routes]
            [flashcards.server.response :as r]
            [flashcards.server.middleware :as m]
            [shadow.http.push-state :as push-state]
            [taoensso.timbre :as log]))

;; NOTE - Compojure style
;;
#_(c/defroutes routes
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

#_(def dev-handler (-> #'routes
                     m/wrap-reload
                     push-state/handle))

#_(def handler (-> routes
                 m/wrap-parse-edn-body))

;; NOTE - Bidi style
;; TODO - Need to see how this handles reloads / repl changes
(def ^:private handlers
  {:app/root {:get (fn [_] (r/resource "index.html" {:root "public"}))}
   :app/resource {:get (fn [{{resource :resource} :route-params}]
                         (r/resource-with-mime-type resource {:root "public"}))}
   :api/ping {:get (fn [_] (api/ping))}
   :api/flashcards {:get (fn [_] (api/get-flashcards))
                    :post api/create-flashcard
                    :delete api/delete-flashcard
                    :options (partial api/access-control :flashcards)}
   :api/decks {:get (fn [_] (api/get-decks))
               :post api/create-deck
               :delete api/delete-deck
               :options (partial api/access-control :decks)}})

(def ^:private ring-handler
  (fn [{:keys [uri request-method] :as req}]
    (let [{:keys [handler route-params]} (routes/uri->route uri)
          route-handlers (get handlers handler)
          f (get route-handlers request-method)]
      (if f
        (f (cond-> req
             route-params (assoc :route-params route-params)))
        (r/not-found {:uri uri :method request-method})))))

(def dev-handler (-> #'ring-handler
                     m/wrap-reload
                     push-state/handle))

(def handler (-> ring-handler
                 m/wrap-parse-edn-body))

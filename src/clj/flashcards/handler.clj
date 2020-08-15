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
  {:app/root (fn [_] (r/resource "index.html" {:root "public"}))
   :app/resource (fn [{{resource :resource} :route-params}]
                   (r/resource-with-mime-type resource {:root "public"}))
   :api/ping (fn [_] (api/ping))
   :api/echo api/echo
   :api/flashcards (fn [{:keys [request-method] :as req}]
                     (case request-method
                       :options (api/access-control :flashcards req)
                       :get (api/get-flashcards)
                       :post (api/create-flashcard req)
                       :delete (api/delete-flashcard req)))
   nil (fn [_] (r/not-found nil))})

(def ^:private ring-handler
  (fn [{:keys [uri request-method] :as req}]
    (let [{:keys [handler
                  route-params
                  methods]} (routes/uri->route uri)
          f (get handlers handler)]
      (if (contains? (set methods) request-method)
        (f (cond-> req
             route-params (assoc :route-params route-params)))
        (r/not-found {:method request-method})))))

(def dev-handler (-> #'ring-handler
                     m/wrap-reload
                     push-state/handle))

(def handler (-> ring-handler
                 m/wrap-parse-edn-body))

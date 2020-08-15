(ns flashcards.routes
  (:require [bidi.bidi :as bidi]
            [taoensso.timbre :as log]))

(def ^:private router
  ["/"
   [["" ::app-root]
    ["api"
     [["/ping" ::api-ping]
      ["/echo" ::api-echo]
      ["/flashcards" ::api-flashcards]]]
    [[:resource] ::app-resource]]])

;; TODO - Do we want the :methods in here?
;; Or do they belong in flashcards.handler?
(def ^:private routes
  {::api-ping {:handler :api/ping
               :methods [:get]}
   ::api-echo {:handler :api/echo
               :methods [:post]}
   ::api-flashcards {:handler :api/flashcards
                     :methods [:options :get :post :delete]}
   ::app-root {:handler :app/root
               :methods [:get]}
   ::app-resource {:handler :app/resource
                   :methods [:get]}
   nil {:handler :not-found}})

(defn uri->route
  [uri]
  (let [{route-key :handler
         route-params :route-params} (bidi/match-route router uri)]
    (cond-> (get routes route-key)
      route-params (assoc :route-params route-params))))

(defn route->uri
  [route route-params]
  (apply (partial bidi/path-for router) route (mapcat seq route-params)))

(comment

  router
  (bidi/match-route router "/api/ping")
  (uri->route "/api/ping")
  (uri->route "/api/echo")
  (uri->route "/api/foo")

  (uri->route "/blah.js")
  (apply println {:a 1})
  (mapcat seq {:a 1 :b 2})

  (route->uri ::app-resource {:resource "blah.js"})
  )

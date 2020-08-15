(ns flashcards.server.middleware
  (:require [clojure.edn :as edn]
            [ring.middleware.reload :as rm]
            [taoensso.timbre :as log]))

(def wrap-reload rm/wrap-reload)

(defn wrap-parse-edn-body
  [handler]
  (fn [{:keys [body] :as request}]
    (handler
     (if body
       (-> request
           (assoc :edn-body (edn/read-string (slurp body)))
           (dissoc :body))
       request))))

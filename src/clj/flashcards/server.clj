(ns flashcards.server
  (:require [flashcards.handler :refer [handler]]
            [flashcards.config :as config]
            [ring.adapter.jetty :refer [run-jetty]])
  (:gen-class))

(defn start
  [handler port]
  (run-jetty handler {:port port :join? false}))

(defn -main [& _args]
  (config/with-config (config/read-config)
    (start handler (config/server-port))))

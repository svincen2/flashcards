(ns flashcards.server.response
  (:require [ring.util.response :as r]
            [taoensso.timbre :as log]
            [clojure.string :as str]))

(defn edn-response
  [body]
  (-> (r/response body)
      (r/content-type "application/edn")
      (r/header "Access-Control-Allow-Origin" "*")))

(def resource r/resource-response)

(defn ok
  [body]
  (edn-response (pr-str body)))

(defn not-found
  [body]
  (-> (edn-response body)
      (r/status 404)))

(defn access-control
  [origin {:keys [methods headers]}]
  (cond-> (edn-response nil)
    :always (r/header "Access-Control-Allow-Origin" origin)
    methods (r/header "Access-Control-Allow-Methods" (str/join "," methods))
    headers (r/header "Access-Control-Allow-Headers" (str/join "," headers))))

(defn forbidden
  [body]
  (-> (edn-response body)
      (r/status 403)))

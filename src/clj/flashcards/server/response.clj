(ns flashcards.server.response
  (:require [ring.util.mime-type :as mime]
            [ring.util.response :as r]
            [taoensso.timbre :as log]
            [clojure.string :as str]))

;; convenience 'aliases'
(def resource r/resource-response)
(def content-type r/content-type)
(def header r/header)
(def status r/status)

(defn edn-response
  [body]
  (-> (r/response body)
      (content-type "application/edn")
      (header "Access-Control-Allow-Origin" "*")))

(defn ok
  [body]
  (edn-response (pr-str body)))

(defn not-found
  [body]
  (-> (edn-response body)
      (status 404)))

(defn access-control
  [origin {:keys [methods headers]}]
  (cond-> (edn-response nil)
    :always (header "Access-Control-Allow-Origin" origin)
    methods (header "Access-Control-Allow-Methods" (str/join "," methods))
    headers (header "Access-Control-Allow-Headers" (str/join "," headers))))

(defn forbidden
  [body]
  (-> (edn-response body)
      (status 403)))

(defn resource-with-mime-type
  [file root]
  (let [mime-type (mime/ext-mime-type file)]
    (-> (resource file root)
        (content-type mime-type))))

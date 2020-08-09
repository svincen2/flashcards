(ns flashcards.config
  (:require [aero.core :as aero]
            [clojure.java.io :as io]
            [taoensso.timbre :as log]))

(def ^:dynamic *config* nil)

(def ^:private config-file "config.edn")

(defn ^:private get-default-profile
  []
  (keyword (or (System/getenv "FLASHCARDS_PROFILE")
               (System/getProperty "flashcards.profile")
               "dev")))

(defn read-config
  ([]
   (read-config config-file))
  ([file]
   (read-config file (get-default-profile)))
  ([file profile]
   (aero/read-config (io/resource file) {:profile profile})))

(defmacro with-config
  [config & body]
  `(binding [*config* ~config]
     ~@body))

;; Specific config access

(defn server-port
  ([]
   (server-port *config*))
  ([config]
   (:server/port config)))



(comment
  (log/info :keep-me-in-the-ns)
 
  (read-config config-file :dev)
  ;; ... same as ...
  (read-config config-file)
  ;; ... same as ...
  (read-config)

  (server-port (read-config))

  (with-config (read-config)
    (server-port))
  )

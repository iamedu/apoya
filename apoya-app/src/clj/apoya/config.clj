(ns apoya.config
  (:require [nomad :refer [defconfig]]
            [clojure.tools.logging :as log]
            [clojure.java.io :as io]))

(def ^:dynamic *current-site* :default)
(def ^:dynamic *language* :en)

(defonce database (atom nil))

(declare apoya-config)

(defn load-config [config]
  (defconfig apoya-config (io/file config))
  (doseq [[k v] (get (apoya-config) :system-properties)]
    (log/info "Setting property" k v)
    (System/setProperty (name k) v)))

(ns apoya.data.schema
  (:require [clojure.tools.logging :as log])
  (:use korma.db
        korma.core))

(defentity sites
  (pk :domain))

(defentity languages)

(defentity labels)

(defn setup-database [config]
  (log/info "Loading database config" (:db config))
  (default-connection (create-db config)))

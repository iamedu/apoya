(ns apoya.data.schema
  (:require [apoya.config :as cfg]
            [clojure.tools.logging :as log])
  (:use korma.db
        korma.core))

(defentity sites
  (pk :domain))

(defentity languages)

(defentity labels)

(defentity error-sources
  (table :error_sources)
  (pk :name))

(defentity errors
  (pk :error_sha1))

(defentity error-events
  (table :error_events)
  (pk :event_sha1))

(defn enum-cast [x as]
  (raw (format "CAST('%s' AS %s)" (name x) (name as))))

(defn setup-database [config]
  (log/info "Loading database config" (:db config))
  (let [db (create-db config)]
    (reset! cfg/database db)
    (default-connection db)))

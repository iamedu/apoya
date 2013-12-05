(ns apoya.data.schema
  (:require [apoya.config :as cfg]
            [clojure.tools.logging :as log]
            [korma.sql.engine :refer [infix]])
  (:import [org.postgresql.util PGobject])
  (:use korma.db
        korma.core))

(defentity impersonate_permissions)

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
  (pk :event_sha1)
  (belongs-to errors {:fk :error_sha1})
  (belongs-to error-sources {:fk :error_source})
  (transform (fn [{metadata :metadata :as v}]
               (if (and metadata
                        (instance? PGobject metadata))
                 (assoc v :metadata (.getValue metadata))
                 v))))

(defentity restricted_urls
  (pk :url))

(defentity permissions
  (pk :permission))

(defentity roles
  (pk :role_code)
  (many-to-many permissions :role_permissions
                {:lfk "role_permissions.role_code"
                 :rfk "role_permissions.permission"})
  (many-to-many restricted_urls :role_urls
                {:lfk "role_urls.role_code"
                 :rfk "role_urls.url"}))

(defentity users
  (pk :username)
  (transform (fn [{status :status :as v}]
               (if (and status
                        (instance? PGobject status))
                 (assoc v :status (.getValue status))
                 v)))
  (many-to-many roles :role_assignments
                {:lfk "role_assignments.username"
                 :rfk "role_assignments.role_code"})
  (many-to-many permissions :person_permissions
                {:lfk "person_permissions.username"
                 :rfk "person_permissions.permission"})
  (many-to-many permissions :person_permissions
                {:lfk "person_permissions.username"
                 :rfk "person_permissions.permission"})
  (many-to-many restricted_urls :person_urls
                {:lfk "person_urls.username"
                 :rfk "person_urls.url"}))

(defentity base_mail_templates)

(defentity mail_templates)

(defn pred-tilde [k v] (infix k "~" v))

(defn enum-cast [x as]
  (raw (format "CAST('%s' AS %s)" (name x) (name as))))

(defn json-cast [x]
  (if x
    (raw (format "'%s'::json" (.replaceAll x "'" "''")))))

(defn setup-database [config]
  (let [config (or (cfg/db-env-config config)
                   config)
        db (create-db config)]
    (log/info "Connecting to database" (:subname config))
    (reset! cfg/database db)
    (default-connection db)))

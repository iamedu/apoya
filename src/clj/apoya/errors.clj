(ns apoya.errors
  (:require [apoya.config :as cfg]
            [fortress.util :as util]
            [cemerick.friend :as friend]
            [cheshire.core :as json]
            [clj-stacktrace.repl :as st]
            [clojure.stacktrace :as sta]
            [clojure.tools.logging :as log])
  (:use korma.core
        apoya.data.schema)
  (:import [java.io PrintStream ByteArrayOutputStream]))

(defn str-throwable [cause]
  (st/pst-str cause))

(defmulti write-error (fn [database _]
                        (get-in database [:options :subprotocol])))

(defn find-or-insert-error! [error-string error_type]
  (let [error-sha1 (util/sha1 error-string)]
    (or (first (select errors
                       (where {:error_sha1 error-sha1})))
        (insert errors
                (values {:error_sha1 error-sha1
                         :error_text error-string})))))

(defmethod write-error "postgresql"
  [_ {:keys [event_sha1 error_sha1 severity error_source metadata domain username]}]
  (insert error-events
          (values {:metadata (json-cast metadata)
                   :event_sha1 event_sha1
                   :error_sha1 error_sha1
                   :username username
                   :domain domain
                   :severity (enum-cast severity :Error_Severity)
                   :error_source error_source})))

(defn webapp-error [request cause]
  (let [error-string (str-throwable cause)
        json-meta (json/generate-string (dissoc request :flash :session :body :access-rules))
        {:keys [error_sha1]} (find-or-insert-error! error-string "http")
        event-sha1 (util/random-sha1)
        id (-> (friend/current-authentication request) :identity)]
    (log/warn cause "There was an unhandled error for site" cfg/*current-site* "and url" (:uri request))
    (write-error @cfg/database
                 {:metadata json-meta
                  :event_sha1 event-sha1
                  :error_sha1 error_sha1
                  :domain (name cfg/*current-site*)
                  :username id
                  :severity :WARNING
                  :error_source "webapp"})
    event-sha1))

(defn fortress-error [_ cause]
  (let [error-string (str-throwable cause)
        {:keys [error_sha1]} (find-or-insert-error! error-string "fortress")
        event-sha1 (util/random-sha1)]
    (log/fatal cause "An error wasn't handled by the webapp, check what's happening NOW.")
    (write-error @cfg/database
                 {:event_sha1 event-sha1
                  :error_sha1 error_sha1
                  :severity :FATAL
                  :error_source "netty"})))


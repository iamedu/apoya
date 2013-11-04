(ns apoya.errors
  (:require [apoya.config :as cfg]
            [fortress.util :as util]
            [clojure.stacktrace :as sta]
            [clojure.tools.logging :as log])
  (:use korma.core
        apoya.data.schema)
  (:import [java.io PrintStream ByteArrayOutputStream]))

(defn str-throwable [cause]
  (with-open [output-stream (ByteArrayOutputStream.)
              print-stream (PrintStream. output-stream true "UTF-8")]
    (.printStackTrace cause print-stream)
    (.toString output-stream)))

(defmulti write-error (fn [database _]
                        (get-in database [:options :subprotocol])))

(defn find-error [error-string]
  (let [error-sha1 (util/sha1 error-string)]
    (or (first (select errors
                       (where {:error_sha1 error-sha1})))
        (insert errors
                (values {:error_sha1 error-sha1
                         :error_text error-string})))))

(defmethod write-error "postgresql"
  [_ {:keys [event_sha1 error_sha1 severity error_source]}]
  (insert error-events
          (values {:event_sha1 event_sha1
                   :error_sha1 error_sha1
                   :severity (enum-cast severity :Error_Severity)
                   :error_source error_source})))

(defn fortress-error [_ cause]
  (let [error-string (str-throwable cause)
        {:keys [error_sha1]} (find-error error-string)
        event-sha1 (util/random-sha1)]
    (log/fatal cause "An error wasn't handled by the webapp, check what's happening NOW.")
    (write-error @cfg/database
                 {:event_sha1 event-sha1
                  :error_sha1 error_sha1
                  :severity :FATAL
                  :error_source "netty"})))


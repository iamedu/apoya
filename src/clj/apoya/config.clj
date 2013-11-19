(ns apoya.config
  (:use environ.core)
  (:require [nomad :refer [defconfig]]
            [clojure.tools.logging :as log]
            [clojure.java.io :as io]))

(def ^:dynamic *current-site* :default)
(def ^:dynamic *language* :en)

(defonce database (atom nil))

(declare apoya-config)

(def subprotocols
  {"postgres" "postgresql"})

(defn db-env-config [{:keys [env-variable] :as config}]
  (when env-variable
    (let [db-regex #"(\w+)://(\w+):(\w*)@([A-Za-z0-9\.\-]+):(\d+)/(\w+)"
          url (-> env-variable
                  (clojure.string/lower-case)
                  (clojure.string/replace #"_" "-")
                  (keyword)
                  (env))
          [_ subprotocol username password host port db-name] (re-matches db-regex url)
          config (dissoc config :env-variable)]
      (merge config {:subprotocol (get subprotocols subprotocol subprotocol)
                     :user username
                     :password password
                     :subname (str "//" host ":" port "/" db-name)}))))


(defn load-config [config]
  (defconfig apoya-config (io/file config))
  (doseq [[k v] (get (apoya-config) :system-properties)]
    (log/info "Setting property" k v)
    (System/setProperty (name k) v)))

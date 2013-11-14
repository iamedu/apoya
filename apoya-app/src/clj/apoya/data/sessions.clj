(ns apoya.data.sessions
  (:require [apoya.config :as cfg]
            [fortress.util :as futil]
            [clojure.java.jdbc :as jdbc]
            [clojure.tools.logging :as log])
  (:import [java.sql DriverManager]
           [java.util Properties Date]))

(def sessions (atom {}))

(defn- ^Properties as-properties
  "Convert any seq of pairs to a java.utils.Properties instance.
   Uses sql/as-str to convert both keys and values into strings."
  [m]
  (let [p (Properties.)]
    (doseq [[k v] m]
      (.setProperty p (name k) (str v)))
    p))

(defn- prepare-session [conn]
  (let [uuid (futil/random-uuid-str)]
    (.setAutoCommit conn false)
    {:uuid uuid
     :creation-date (Date.)
     :conn conn}))

(defn create-session []
  (let [{:keys [subprotocol subname classname] :as db-spec} (:db (cfg/apoya-config))
        url  (format "jdbc:%s:%s" subprotocol subname)
        etc  (dissoc db-spec :classname :subprotocol :subname)]
    (clojure.lang.RT/loadClassForName classname)
    (let [session (prepare-session (DriverManager/getConnection url (as-properties etc)))]
      (swap! sessions assoc (:uuid session) session)
      session)))



(ns apoya.data.sessions
  (:require [apoya.config :as cfg]
            [fortress.util :as futil]
            [clojure.java.jdbc :as jdbc]
            [clojure.tools.logging :as log])
  (:import [java.sql DriverManager]
           [java.util Properties Date]))

(defonce sessions (atom {}))

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
     :last-used (Date.)
     :conn conn}))

(defn create-session []
  (let [{:keys [subprotocol subname classname] :as db-spec} (:db (cfg/apoya-config))
        url  (format "jdbc:%s:%s" subprotocol subname)
        etc  (dissoc db-spec :classname :subprotocol :subname)]
    (clojure.lang.RT/loadClassForName classname)
    (let [session (prepare-session (DriverManager/getConnection url (as-properties etc)))]
      (swap! sessions assoc (:uuid session) session)
      session)))

(defn touch-session [uuid]
  (swap! sessions assoc-in [uuid :last-used] (Date.)))

(defn exec-sql [uuid sql]
  (let [{:keys [conn]} (get @sessions uuid)
        statement (.createStatement conn)
        result-set? (.execute statement sql)]
    (touch-session uuid)
    (if result-set?
      {:result-set (jdbc/resultset-seq (.getResultSet statement)) :type :result-set}
      {:update-count (.getUpdateCount statement) :type :update-count})))


(defn commit-session [uuid]
  (let [{conn :conn} (get @sessions uuid)]
    (touch-session uuid)
    (.commit conn)))

(defn rollback-session [uuid]
  (let [{conn :conn} (get @sessions uuid)]
    (touch-session uuid)
    (.rollback conn)))

(defn close-session [uuid]
  (let [{conn :conn} (get @sessions uuid)]
    (.close conn)
    (swap! sessions dissoc uuid)))

(defn close-old-sessions [date seconds]
  (let [old-session? #(> (- (.getTime date) (.getTime (:last-used %))) (* seconds 1000))
        sessions @sessions]
    (doseq [[_ session] sessions]
      (if (old-session? session)
        (close-session (:uuid session))))))


(ns apoya.data.sessions
  (:require [apoya.config :as cfg]
            [fortress.util :as futil]
            [clojure.java.jdbc :as jdbc]
            [clojure.tools.logging :as log])
  (:import [java.sql DriverManager]
           [java.util Properties Date]
           [org.postgresql.util PGobject]
           ))

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

(defn close-rset [uuid]
  (let [{:keys [result-set]} (get @sessions uuid)]
    (when result-set
      (swap! sessions update-in [uuid] dissoc :rset :result-set)
      (.close result-set))))

(defn exec-sql [uuid sql]
  (let [{:keys [conn]} (get @sessions uuid)
        statement (.createStatement conn)
        result-set? (.execute statement sql)
        result-set (.getResultSet statement)
        rset-seq (if result-set? (jdbc/resultset-seq (.getResultSet statement)))]
    (close-rset uuid)
    (swap! sessions assoc-in [uuid :text] sql)
    (swap! sessions assoc-in [uuid :rset] rset-seq)
    (swap! sessions assoc-in [uuid :result-set] result-set)
    (touch-session uuid)
    (if result-set?
      {:type :result-set}
      {:update-count (.getUpdateCount statement) :type :update-count})))

(defn secure-convert-results [results]
  (let [convert-cols (fn [[k v]]
                       (cond 
                         (instance? (clojure.lang.RT/classForName "[B") v) [k (String. v)]
                         (instance? PGobject v) [k (.getValue v)]
                         :else [k v]))]
    (for [r results]
      (apply hash-map (-> (map convert-cols r) flatten)))))

(defn stream-results [uuid size]
  (let [{:keys [conn rset]} (get @sessions uuid)
        current-rset (take size rset)
        new-rset (drop size rset)]
    (when (empty? new-rset)
      (close-rset uuid))
    (when-not (empty? new-rset)
      (swap! sessions assoc-in [uuid :rset] new-rset))
    {:result (secure-convert-results current-rset)}))

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
    (close-rset uuid)
    (rollback-session uuid)
    (.close conn)
    (swap! sessions dissoc uuid)))

(defn close-old-sessions [date seconds]
  (let [old-session? #(> (- (.getTime date) (.getTime (:last-used %))) (* seconds 1000))
        sessions @sessions]
    (doseq [[_ session] sessions]
      (if (old-session? session)
        (close-session (:uuid session))))))


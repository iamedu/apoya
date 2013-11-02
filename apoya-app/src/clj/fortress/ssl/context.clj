(ns fortress.ssl.context
  (:import [javax.net.ssl KeyManagerFactory SSLContext]))

(def ^:dynamic protocol "TLS")

(defn ssl-context [key-store passphrase-chars]
  (let [key-manager-algo (KeyManagerFactory/getDefaultAlgorithm)
        key-manager-factory (KeyManagerFactory/getInstance key-manager-algo)
        ssl-context (SSLContext/getInstance protocol)]
    (.init key-manager-factory key-store passphrase-chars)
    (.init ssl-context (.getKeyManagers key-manager-factory) nil nil)
    ssl-context))

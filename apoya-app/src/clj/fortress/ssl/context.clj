(ns fortress.ssl.context
  (:require [fortress.ssl.certs :as certs]
            [fortress.util :as util]
            [clojure.java.io :as io])
  (:import [javax.net.ssl KeyManagerFactory SSLContext]))

(def ^:dynamic protocol "TLS")

(defn ssl-context [key-store passphrase-chars]
  (let [key-manager-algo (KeyManagerFactory/getDefaultAlgorithm)
        key-manager-factory (KeyManagerFactory/getInstance key-manager-algo)
        ssl-context (SSLContext/getInstance protocol)]
    (.init key-manager-factory key-store passphrase-chars)
    (.init ssl-context (.getKeyManagers key-manager-factory) nil nil)
    ssl-context))

(defn build-ssl-context [key-file crt-file read-password]
  (let [cert (-> crt-file
                 (io/input-stream)
                 (certs/load-pem-object)
                 (certs/gen-cert))
        prospect-key (-> key-file
                         (io/input-stream)
                         (certs/load-pem-object))
        private-key (if (certs/encrypted-pem-key? prospect-key)
                      (certs/decrypt-pem-key prospect-key (read-password))
                      prospect-key)
        key-pair (certs/gen-key-pair private-key)
        password-chars (.toCharArray (util/random-uuid-str))
        key-store (certs/gen-key-store key-pair cert password-chars)]
    (ssl-context key-store password-chars)))


(ns apoya.core
  (:use compojure.core)
  (:require [clojure.tools.cli :refer [cli]] 
            [clojure.tools.logging :as log]
            [clojure.tools.nrepl.server :as nrepl]
            [clojure.java.io :as io]
            [fortress.ring.server :as fortress]
            [fortress.ssl.certs :as certs]
            [fortress.ssl.context :as context]
            [fortress.util :as util]
            [compojure.route :as route]
            [nomad :refer [defconfig]])
  (:import [java.awt GraphicsEnvironment]
           [javax.swing JPasswordField JOptionPane]
           [ch.qos.logback.classic.joran JoranConfigurator]
           [ch.qos.logback.core.util StatusPrinter]
           [org.slf4j LoggerFactory])
  (:gen-class))

(declare apoya-config)

(defroutes app 
  (GET "/"  [] "<h1>Hello World</h1>")
  (route/not-found "<h1>Page not found</h1>"))

(defn load-logback [logback-file]
  (let [context (doto (LoggerFactory/getILoggerFactory)
                  (.reset))
        configurator (doto (JoranConfigurator.)
                       (.setContext context))]
    (.doConfigure configurator logback-file)
    (StatusPrinter/printInCaseOfErrorsOrWarnings context)))

(defn read-password []
  (cond
    (not (GraphicsEnvironment/isHeadless)) (let [pf (JPasswordField.)
                                                 result (JOptionPane/showConfirmDialog nil pf
                                                                                       "Fortress: Enter certificate password"
                                                                                       JOptionPane/OK_CANCEL_OPTION
                                                                                       JOptionPane/PLAIN_MESSAGE)]
                                             (if (= result JOptionPane/OK_OPTION)
                                               (.getPassword pf)))
    (not (nil? (System/console))) (.readPassword (System/console) "Private key password: " (object-array 0))))

(defn build-ssl-context [key-file crt-file]
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
    (context/ssl-context key-store password-chars)))

(defn start-system []
  (let [{:keys [nrepl-port http logback-file ssl]} (apoya-config)
        {:keys [key-file crt-file]} ssl
        fortress-config (assoc http
                               :ssl-context (build-ssl-context key-file crt-file))]
    (when logback-file
      (load-logback logback-file))
    (when nrepl-port
      (log/info "Starting nrepl server at port" nrepl-port)
      (nrepl/start-server :port nrepl-port))
    (fortress/run-fortress app fortress-config)))

(defn -main [& args]
  (let [[options args banner] (cli args
                                   ["-h" "--help" "Show this help" :default false :flag true]
                                   ["-c" "--config" "Configuration file" :default "data/config.edn"])
        {:keys [config help]} options]
    (when help
      (println banner)
      (System/exit 0))
    (defconfig apoya-config (io/file config))
    (start-system)))


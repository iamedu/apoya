(ns apoya.core
  (:use compojure.core)
  (:require [clojure.tools.cli :refer [cli]] 
            [clojure.tools.logging :as log]
            [clojure.tools.nrepl.server :as nrepl]
            [clojure.java.io :as io]
            [fortress.ring.server :as fortress]
            [fortress.ssl.context :as ssl-ctx]
            [fortress.util :as util]
            [compojure.route :as route]
            [nomad :refer [defconfig]])
  (:import [ch.qos.logback.classic.joran JoranConfigurator]
           [ch.qos.logback.core.util StatusPrinter]
           [org.slf4j LoggerFactory])
  (:gen-class))

(declare apoya-config)

(defroutes app 
  (GET "/"  [] "<h1>Hello World</h1>")
  (GET "/adios" [] (/ 1 0))
  (route/not-found "<h1>Page not found</h1>"))

(defn load-logback [logback-file]
  (let [context (doto (LoggerFactory/getILoggerFactory)
                  (.reset))
        configurator (doto (JoranConfigurator.)
                       (.setContext context))]
    (.doConfigure configurator logback-file)
    (StatusPrinter/printInCaseOfErrorsOrWarnings context)))

(defn start-system []
  (let [{:keys [nrepl-port http logback-file ssl]} (apoya-config)
        {:keys [key-file crt-file]} ssl
        fortress-config (assoc http
                               :ssl-context (ssl-ctx/build-ssl-context key-file crt-file util/read-password))]
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


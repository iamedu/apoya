(ns apoya.core
  (:use compojure.core)
  (:require [apoya.config :as cfg]
            [apoya.app :refer [app]]
            [apoya.errors :as errors]
            [apoya.data.schema :as schema]
            [apoya.resources.less :as less]
            [apoya.resources.fs :as fs]
            [apoya.monitor.files :as files]
            [clojure.tools.cli :refer [cli]] 
            [clojure.tools.logging :as log]
            [clojure.tools.nrepl.server :as nrepl]
            [fortress.ring.server :as fortress]
            [fortress.ssl.context :as ssl-ctx]
            [fortress.util :as util]
            [nomad :refer [defconfig]])
  (:import [ch.qos.logback.classic.joran JoranConfigurator]
           [ch.qos.logback.core.util StatusPrinter]
           [org.slf4j LoggerFactory])
  (:gen-class))

(defn load-logback [logback-file]
  (let [context (doto (LoggerFactory/getILoggerFactory)
                  (.reset))
        configurator (doto (JoranConfigurator.)
                       (.setContext context))]
    (.doConfigure configurator logback-file)
    (StatusPrinter/printInCaseOfErrorsOrWarnings context)))

(defn start-system []
  (let [{:keys [nrepl-port cljs-nrepl-port http logback-file ssl temp-path]} (cfg/apoya-config)
        {:keys [key-file crt-file]} ssl
        fortress-config (assoc http
                               :listener-builder files/build-upload-listener
                               :temp-path temp-path
                               :error-fn #'errors/fortress-error
                               :ssl-context (ssl-ctx/build-ssl-context key-file crt-file util/read-password))]
    (when logback-file
      (load-logback logback-file))
    (fs/setup-blobstore (get (cfg/apoya-config) :blobstore))
    (schema/setup-database (get (cfg/apoya-config) :db))
    (fortress/run-fortress #'app fortress-config)
    (when nrepl-port
      (log/info "Starting nrepl server at port" nrepl-port)
      (nrepl/start-server :port nrepl-port) 
      (.mkdirs (java.io.File. "target/repl"))
      (spit "target/repl/repl-port" nrepl-port))
    (when cljs-nrepl-port
      (log/info "Starting cljs nrepl server at port" cljs-nrepl-port)
      (nrepl/start-server :port cljs-nrepl-port))
    (less/watch-folders (get (cfg/apoya-config) :less))))

(defn -main [& args]
  (let [[options args banner] (cli args
                                   ["-h" "--help" "Show this help" :default false :flag true]
                                   ["-c" "--config" "Configuration file" :default "data/config.edn"])
        {:keys [config help]} options]
    (when help
      (println banner)
      (System/exit 0))
    (cfg/load-config config)
    (start-system)))


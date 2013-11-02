(ns apoya.core
  (:require [clojure.tools.cli :refer [cli]] 
            [clojure.tools.logging :as log]
            [clojure.tools.nrepl.server :as nrepl]
            [clojure.java.io :as io]
            [nomad :refer [defconfig]])
  (:import [ch.qos.logback.classic.joran JoranConfigurator]
           [ch.qos.logback.core.util StatusPrinter]
           [org.slf4j LoggerFactory])
  (:gen-class))

(declare apoya-config)

(defn load-logback [logback-file]
  (let [context (doto (LoggerFactory/getILoggerFactory)
                  (.reset))
        configurator (doto (JoranConfigurator.)
                       (.setContext context))]
    (.doConfigure configurator logback-file)
    (StatusPrinter/printInCaseOfErrorsOrWarnings context)))

(defn start-system []
  (let [{:keys [nrepl-port http https logback-file]} (apoya-config)]
    (when logback-file
      (load-logback logback-file))
    (when nrepl-port
      (log/info "Starting nrepl server at port" nrepl-port)
      (nrepl/start-server :port nrepl-port))))

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

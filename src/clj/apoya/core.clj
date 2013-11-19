(ns apoya.core
  (:use compojure.core
        environ.core)
  (:require [apoya.config :as cfg]
            [apoya.app :refer [app]]
            [apoya.errors :as errors]
            [apoya.data.schema :as schema]
            [apoya.resources.less :as less]
            [apoya.resources.fs :as fs]
            [apoya.monitor.files :as files]
            [apoya.services.schedule :as schedule]
            [apoya.util.classloader :refer [with-classloader]]
            [clojure.tools.cli :refer [cli]] 
            [clojure.tools.logging :as log]
            [clojure.tools.nrepl.server :as nrepl]
            [fortress.ring.server :as fortress]
            [fortress.ssl.context :as ssl-ctx]
            [fortress.util :as util]
            [cemerick.piggieback :as pback]
            [nomad :refer [defconfig]])
  (:import [apoya.classloader JCloudsClassLoader]
           [ch.qos.logback.classic.joran JoranConfigurator]
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

(defn preload-app []
  (with-classloader (JCloudsClassLoader.)
    (try 
      (when-let [routes (-> "apoya/routes.clj"
                          (clojure.java.io/resource)
                          (slurp))]
        (load-string routes)
        (log/info "Preloaded app from apoya/routes.clj")) 
      (catch Exception _
        (log/info "App (apoya/routes.clj) not found yet in classloader")))))

(defn load-env-ports [{:keys [env-port env-ssl-port] :as http}]
  (let [read-var #(-> %
                      (clojure.string/lower-case)
                      (clojure.string/replace #"_" "-")
                      (keyword)
                      (env)
                      (Integer.))
        http (dissoc http :env-port :env-ssl-port)
        http (if env-port
                 (assoc http :port (read-var env-port))
                 http)
        http (if env-ssl-port
                 (assoc http :ssl-port (read-var env-ssl-port))
                 http)]
    http))

(defn start-system []
  (let [{:keys [nrepl-port cljs-nrepl-port http logback-file ssl temp-path]} (cfg/apoya-config)
        {:keys [key-file crt-file]} ssl
        http (load-env-ports http)
        fortress-config (assoc http
                               :listener-builder files/build-upload-listener
                               :temp-path temp-path
                               :error-fn #'errors/fortress-error
                               :ssl-context (ssl-ctx/build-ssl-context key-file crt-file util/read-password))]
    (when logback-file
      (load-logback logback-file))
    (fs/setup-blobstore (get (cfg/apoya-config) :blobstore))
    (schema/setup-database (get (cfg/apoya-config) :db))
    (preload-app)
    (fortress/run-fortress #'app fortress-config)
    (schedule/setup-scheduler (get (cfg/apoya-config) :db))
    (when nrepl-port
      (log/info "Starting nrepl server at port" nrepl-port)
      (nrepl/start-server :port nrepl-port
                          :handler (nrepl/default-handler #'pback/wrap-cljs-repl))
      (.mkdirs (java.io.File. "target/repl"))
      (spit "target/repl/repl-port" nrepl-port))
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


(ns apoya.routes.command
  (:use compojure.core)
  (:require [apoya.response :as r]
            [apoya.util :as util]
            [apoya.data.sessions :as sess]
            [apoya.resources.fs :as fs]
            [apoya.services.scripting :as scripting]
            [markdown.core :as md]
            [clojure.edn :as e]
            [clojure.java.io :as io]
            [clojure.tools.logging :as log])
  (:import [java.lang.management ManagementFactory]))

(defroutes fs-routes
  (POST "/list-contents.edn" [folder]
        (r/edn-response (fs/list-contents folder))))

(defroutes scripting-routes
  (POST "/create-session.edn" [engine-name]
        (let [sess (scripting/create-session engine-name)]
          (r/edn-response (dissoc sess :engine))))
  (POST "/eval-line.edn" [uuid line]
        (r/edn-response (scripting/eval-line uuid line)))
  (POST "/destroy-session.edn" [uuid]
        (scripting/close-session uuid)
        (r/edn-response true))
  (POST "/list-sessions.edn" _
        (let [sessions (map #(dissoc % :engine) (vals @scripting/sessions))]
          (r/edn-response sessions))) 
  (POST "/list-engines.edn" _
        (r/edn-response (scripting/list-engines)))
  (POST "/eval-code.edn" [engine code]
        (r/edn-response (scripting/eval-code engine code))))

;;; Sql
(defroutes sql-routes
  (POST "/create-session.edn" _
        (let [sess (sess/create-session)]
          (r/edn-response (dissoc sess :conn))))
  (POST "/exec-sql.edn" [uuid sql]
        (let [result (sess/exec-sql uuid sql)]
          (r/edn-response result)))
  (POST "/stream-results.edn" [uuid size]
        (let [result (sess/stream-results uuid size)]
          (r/edn-response result)))
  (POST "/commit.edn" [uuid]
        (sess/commit-session uuid)
        (r/edn-response true))
  (POST "/rollback.edn" [uuid]
        (sess/rollback-session uuid)
        (r/edn-response true))
  (POST "/destroy-session.edn" [uuid]
        (sess/close-session uuid)
        (r/edn-response true))
  (POST "/list-sessions.edn" _
        (let [sessions (map #(dissoc % :conn :result-set :rset) (vals @sess/sessions))]
          (r/edn-response sessions))))

(defn jvm-start-time []
  (-> (ManagementFactory/getRuntimeMXBean) (.getStartTime)))

(defroutes main-routes
  (POST "/list-platform-meta.edn" _
        (let [{:keys [commit version]} (-> (io/resource "platform-meta.edn") slurp e/read-string)
              changelog (-> (io/resource "CHANGELOG.md") slurp md/md-to-html-string)]
          (r/edn-response
            {:platform-version commit
             :platform-commit version
             :changelog changelog
             :platform-millis (util/pretty-from-millis (jvm-start-time))}))))

(defroutes command-routes
  (context "/main" [] main-routes)
  (context "/fs" [] fs-routes)
  (context "/sql" [] sql-routes)
  (context "/scripting" [] scripting-routes))

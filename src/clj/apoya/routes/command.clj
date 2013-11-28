(ns apoya.routes.command
  (:use compojure.core)
  (:require [apoya.response :as r]
            [apoya.util :as util]
            [apoya.data.sessions :as sess]
            [apoya.data.site :as site]
            [apoya.data.mail :as mail]
            [apoya.config :as cfg]
            [apoya.resources.fs :as fs]
            [apoya.services.apps :as apps]
            [apoya.services.scripting :as scripting]
            [markdown.core :as md]
            [clojure.edn :as e]
            [clojure.java.io :as io]
            [clojure.tools.logging :as log])
  (:import [java.lang.management ManagementFactory]
           [java.util Date]))

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
             :migrations (site/list-migrations)
             :platform-millis (util/pretty-from-millis (jvm-start-time))}))))

(defroutes app-routes
  (POST "/upload.edn" [files]
        (let [{:keys [tempfile filename]} files
              apps-path (:apps-path (cfg/apoya-config))
              output-file (io/file (str apps-path "/" filename))]
          (io/make-parents output-file)
          (io/copy tempfile output-file)
          (r/edn-response true)))
  (POST "/list-apps.edn" _
        (let [apps-path (:apps-path (cfg/apoya-config))
              files (->> (io/file apps-path)
                         (file-seq)
                         (filter #(.isFile %)))
              file-data (fn [f]
                          {:name (.getName f)
                           :last-modified (java.util.Date. (.lastModified f))})
              files (map file-data files)]
          (r/edn-response files)))
  (POST "/deploy.edn" [path]
        (let [deploy (apps/deploy path)]
          (r/edn-response deploy)))
  (POST "/show-app-detail.edn" [path]
        (let [details (apps/read-details path)
              details (assoc details :path path)]
          (r/edn-response details)))
  (POST "/list-app-meta.edn" _
        (let [{:keys [commit version]} (-> (io/resource "app-meta.edn") slurp e/read-string)
              changelog (-> (io/resource "APP_CHANGELOG.md") slurp md/md-to-html-string)]
          (r/edn-response
            {:app-version version
             :app-commit commit
             :changelog changelog}))))

(defroutes mail-routes
  (POST "/list-base-mails.edn" [limit offset]
        (r/edn-response (mail/list-base-mails limit offset cfg/*current-site* cfg/*language*))))

(defroutes command-routes
  (context "/main" [] main-routes)
  (context "/app" [] app-routes)
  (context "/fs" [] fs-routes)
  (context "/mail" [] mail-routes)
  (context "/sql" [] sql-routes)
  (context "/scripting" [] scripting-routes))

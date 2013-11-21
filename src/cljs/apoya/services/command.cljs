(ns apoya.services.command
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [apoya.remote.request :as r]))

;; Main
(defn platform-meta []
  (r/edn [:post "/api/v1/command/main/list-platform-meta.edn"]))

(defn restart-fortress []
  (r/edn [:post "/api/v1/command/main/restart-fortress.edn"]))

;; App

(defn app-meta []
  (r/edn [:post "/api/v1/command/app/list-app-meta.edn"]))

;; Scripting
(defn list-engines []
  (r/edn [:post "/api/v1/command/scripting/list-engines.edn"]))

(defn create-scripting-session [engine-name]
  (r/edn [:post "/api/v1/command/scripting/create-session.edn"]
         :content {:engine-name engine-name}))

(defn list-scripting-sessions []
  (r/edn [:post "/api/v1/command/scripting/list-sessions.edn"]))

(defn eval-line [uuid line]
  (r/edn [:post "/api/v1/command/scripting/eval-line.edn"]
         :content {:uuid uuid
                   :line line}))

(defn destroy-scripting-session [uuid]
  (r/edn [:post "/api/v1/command/scripting/destroy-session.edn"]
         :content {:uuid uuid}))

(defn eval-code [engine code]
  (r/edn [:post "/api/v1/command/scripting/eval-code.edn"]
         :content {:engine engine
                   :code code}))

;; SQL Sessions
(defn create-session []
  (r/edn [:post "/api/v1/command/sql/create-session.edn"]))

(defn list-sessions []
  (r/edn [:post "/api/v1/command/sql/list-sessions.edn"]))

(defn commit [uuid]
  (r/edn [:post "/api/v1/command/sql/commit.edn"]
         :content {:uuid uuid}))

(defn rollback [uuid]
  (r/edn [:post "/api/v1/command/sql/rollback.edn"]
         :content {:uuid uuid}))

(defn exec-sql [uuid sql]
  (r/edn [:post "/api/v1/command/sql/exec-sql.edn"]
         :content {:uuid uuid
                   :sql sql}))

(defn stream-results [uuid size]
  (r/edn [:post "/api/v1/command/sql/stream-results.edn"]
         :content {:uuid uuid
                   :size size}))

(defn destroy-session [uuid]
  (r/edn [:post "/api/v1/command/sql/destroy-session.edn"]
         :content {:uuid uuid}))

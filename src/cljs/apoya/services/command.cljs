(ns apoya.services.command
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [apoya.remote.request :as r]))

;; Scripting
(defn list-engines []
  (r/edn [:post "/api/v1/command/scripting/list-engines.edn"]))

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

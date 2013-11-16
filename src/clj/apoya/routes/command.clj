(ns apoya.routes.command
  (:use compojure.core)
  (:require [apoya.response :as r]
            [apoya.data.sessions :as sess]
            [apoya.services.scripting :as scripting]
            [clojure.tools.logging :as log]))

(defroutes scripting-routes
  (POST "/list-engines.edn" request
        (r/edn-response (scripting/list-engines)))
  (POST "/eval-code.edn" [engine code]
        (r/edn-response (scripting/eval-code engine code))))

;;; Sql
(defroutes sql-routes
  (POST "/create-session.edn" request
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
  (POST "/list-sessions.edn" request
        (let [sessions (map #(dissoc % :conn :result-set :rset) (vals @sess/sessions))]
          (r/edn-response sessions))))

(defroutes command-routes
  (context "/sql" [] sql-routes)
  (context "/scripting" [] scripting-routes))

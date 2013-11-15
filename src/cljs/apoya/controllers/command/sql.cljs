(ns apoya.controllers.command.sql
  (:require-macros [apoya.angular :refer [defcontroller]]
                   [cljs.core.async.macros :refer [go]])
  (:require [apoya.main :refer [app]]
            [apoya.services.auth :as auth]
            [apoya.services.command :as command]
            [apoya.util.log :as log]
            [apoya.util.angular :refer [oset!]]
            [cljs.core.async :refer [<!]]))

(defn list-sessions [$scope]
  (go
    (let [sessions (:body (<! (command/list-sessions)))]
      (oset! $scope
             :sessions sessions))))

(defn create-session [$scope]
  (go
    (if-not (nil? (<! (command/create-session)))
      (list-sessions $scope)
      (log/info "Cannot create session"))))

(defn destroy-session [$scope s]
  (go
    (when-not (nil? (<! (command/destroy-session (:uuid s))))
      (oset! $scope :session nil :commit nil :rollback nil)
      (list-sessions $scope))))

(defn execute-sql [$scope uuid sql]
  (go
    (let [{:keys [result-set type update-count error]} (:body (<! (command/exec-sql uuid sql)))]
      (oset! $scope :currentDate (js/Date.) :commit nil :rollback nil)
      (condp = type
        :result-set (oset! $scope :resultSet result-set :updateCount nil :error nil :headers (-> result-set first keys))
        :update-count (oset! $scope :resultSet nil :updateCount update-count :error nil)
        :error (oset! $scope :resultSet nil :updateCount nil :error error)))))

(defn commit [$scope uuid]
  (go
    (let [{body :body} (<! (command/commit uuid))]
      (if body
        (oset! $scope :commit true :rollback nil)
        (oset! $scope :commit false :rollback nil)))))

(defn rollback [$scope uuid]
  (go
    (let [{body :body} (<! (command/rollback uuid))]
      (if body
        (oset! $scope :rollback true :commit nil)
        (oset! $scope :rollback false :commit nil)))))

(defn load-cm [cm]
  (defn refresh-cm []
    (js/setTimeout #(.refresh cm) 100)))

(defn select-session [$scope s]
  (oset! $scope
         :commit nil
         :rollback nil
         :session s
         :sqlCode (:text s))
  (refresh-cm))

(defcontroller app CommandSqlCtrl [$scope]
  (oset! $scope
         :section "sql"
         :sqlOptions {:mode "text/x-plsql"
                      :matchBrackets true
                      :autofocus true
                      :lineWrapping true
                      :lineNumbers true
                      :onLoad load-cm}
         :executeSql (partial execute-sql $scope)
         :commitSession (partial commit $scope)
         :rollbackSession (partial rollback $scope)
         :destroySession (partial destroy-session $scope)
         :selectSession (partial select-session $scope)
         :createSession (partial create-session $scope))
  (list-sessions $scope))


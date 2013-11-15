(ns apoya.controllers.command.sql
  (:require-macros [apoya.angular :refer [defcontroller]]
                   [cljs.core.async.macros :refer [go]])
  (:require [apoya.main :refer [app]]
            [apoya.services.auth :as auth]
            [apoya.services.command :as command]
            [apoya.util.log :as log]
            [apoya.util.angular :refer [oset!]]
            [cljs.core.async :refer [<!]]))

(def *size* 20)

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

(defn stream-results [$scope uuid]
  (go
    (let [results (-> (<! (command/stream-results uuid *size*)) :body :result)
          result-set (js->clj (:resultSet $scope))
          total-results (concat result-set results)]
      (oset! $scope :resultSet total-results :headers (filter #(not= % "$$hashKey") (-> total-results first keys))))))

(defn execute-sql [$scope uuid sql]
  (go
    (let [{:keys [result-set type update-count exception]} (:body (<! (command/exec-sql uuid sql)))]
      (oset! $scope :currentDate (js/Date.) :commit nil :rollback nil)
      (condp = type
        :result-set (do
                      (oset! $scope :resultSet [] :exception nil)
                      (stream-results $scope uuid))
        :update-count (oset! $scope :resultSet nil :updateCount update-count :exception nil)
        :exception (oset! $scope :resultSet nil :updateCount nil :exception exception)))))

(defn commit [$scope uuid]
  (go
    (let [{body :body} (<! (command/commit uuid))]
      (if body
        (oset! $scope :commit true :rollback nil :currentDate (js/Date.))
        (oset! $scope :commit false :rollback nil :currentDate (js/Date.))))))

(defn rollback [$scope uuid]
  (go
    (let [{body :body} (<! (command/rollback uuid))]
      (if body
        (oset! $scope :rollback true :commit nil :currentDate (js/Date.))
        (oset! $scope :rollback false :commit nil :currentDate (js/Date.))))))

(defn load-cm [cm]
  (defn refresh-cm []
    (js/setTimeout #(.refresh cm) 100)))

(defn select-session [$scope s]
  (oset! $scope
         :commit nil
         :rollback nil
         :session s
         :resultSet nil
         :currentDate nil
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
         :createSession (partial create-session $scope)
         :streamResults (partial stream-results $scope))
  (list-sessions $scope))

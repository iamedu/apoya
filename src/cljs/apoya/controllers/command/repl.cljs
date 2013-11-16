(ns apoya.controllers.command.repl
  (:require-macros [apoya.angular :refer [defcontroller]]
                   [cljs.core.async.macros :refer [go]])
  (:require [apoya.main :refer [app]]
            [apoya.services.auth :as auth]
            [apoya.services.command :as command]
            [apoya.util.log :as log]
            [apoya.util.angular :refer [oset!]]
            [cljs.core.async :refer [<! take!]]))

(declare handle-repl-command start-repl stop-repl handle-exception) 
(def session (atom nil))
(def controller (atom nil))

(def repl-conf
  {:promptLabel "REPL> "
   :autofocus true
   :animateScroll true
   :promptHistory true
   :commandHandle handle-repl-command})

(defn read-local-sessions []
  (let [local-sessions-str (or (.get js/store "scripting-local-sessions") "nil")]
    (set (cljs.reader/read-string local-sessions-str))))

(defn write-local-sessions [sessions]
  (.set js/store "scripting-local-sessions" (pr-str sessions)))

(defn filter-local-sessions [$scope sessions]
  (let [local-sessions (read-local-sessions)
        {showSessions :showSessions} $scope]
    (if (= showSessions "mine")
      (filter #(local-sessions (:uuid %)) sessions)
      sessions)))

(defn list-sessions [$scope]
  (go
    (let [sessions (:body (<! (command/list-scripting-sessions)))
          sessions (filter-local-sessions $scope sessions)]
      (oset! $scope
             :sessions sessions))))

(defn create-session [$scope engine]
  (go
    (let [uuid (get-in (<! (command/create-scripting-session engine)) [:body :uuid])
          local-sessions (read-local-sessions)
          new-local-sessions (conj local-sessions uuid)]
      (write-local-sessions new-local-sessions)
      (if-not (nil? uuid)
        (list-sessions $scope)
        (log/info "Cannot create session")))))

(defn destroy-session [$scope s]
  (handle-exception nil)
  (go
    (when-not (nil? (<! (command/destroy-scripting-session (:uuid s))))
      (write-local-sessions (disj (read-local-sessions) (:uuid s)))
      (oset! $scope :session nil :commit nil :rollback nil)
      (list-sessions $scope))))

(defn select-session [$scope s]
  (oset! $scope
         :session s)
  (reset! session s)
  (stop-repl)
  (start-repl $scope s))

(defn handle-repl-command [line report]
  (if (and line (not-empty line))
    (go
      (let [{uuid :uuid} @session
            {:keys [result exception exception-message output nspace error-output]}
            (:body (<! (command/eval-line uuid line)))
            res (if (not-empty output) [{:msg output :className "text-success"}])
            res (if (not-empty error-output) (conj res {:msg error-output :className "text-warning"})
                  res)
            res (if (not-empty exception-message) (conj res {:msg exception-message :className "text-danger"})
                  res)
            res (if (not-empty result) (conj res {:msg result :className "text-info"})
                  res)]
        (handle-exception exception)
        ((get @controller "scrollToBottom"))
        (report (clj->js res))))
    (report (clj->js []))))

(defn stop-repl []
  (doto (js/jQuery "#repl")
    (.html "")))

(defn start-repl [$scope session]
  (let [{:keys [nspace]} session
        repl-div (js/jQuery "<div>")
        parent-div (doto (js/jQuery "#repl")
                     (.append repl-div))
        repl (.console repl-div (clj->js (assoc repl-conf
                                                :promptLabel (str nspace "=> "))))]
    (reset! controller repl)))

(defcontroller app CommandReplCtrl [$scope]
  (defn handle-exception [exception]
    (oset! $scope
           :exception exception))
  (oset! $scope
         :section "repl"
         :selectSession (partial select-session $scope)
         :createSession (partial create-session $scope)
         :destroySession (partial destroy-session $scope))
  (go
    (let [engines (:body (<! (command/list-engines)))]
      (oset! $scope
             :scriptingEngines engines))) 
  (.$watch $scope "showSessions" #(list-sessions $scope))
  (list-sessions $scope))



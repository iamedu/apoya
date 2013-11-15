(ns apoya.controllers.command.sql
  (:require-macros [apoya.angular :refer [defcontroller]]
                   [cljs.core.async.macros :refer [go]])
  (:require [apoya.main :refer [app]]
            [apoya.services.auth :as auth]
            [apoya.services.command :as command]
            [apoya.util.log :as log]
            [apoya.util.angular :refer [oset!]]
            [cljs.core.async :refer [<!]]))

(defn load-cm [cm]
  (js/setTimeout
    #(.refresh cm)
    100))

(defcontroller app CommandSqlCtrl [$scope]
  (oset! $scope
         :section "sql"
         :sqlOptions {:mode "text/x-plsql"
                      :matchBrackets true
                      :autofocus true
                      :lineWrapping true
                      :lineNumbers true
                      :onLoad load-cm}))


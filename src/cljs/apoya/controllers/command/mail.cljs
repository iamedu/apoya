(ns apoya.controllers.command.mail
  (:require-macros [apoya.angular :refer [defcontroller]]
                   [cljs.core.async.macros :refer [go]])
  (:require [apoya.main :refer [app]]
            [apoya.services.auth :as auth]
            [apoya.services.command :as command]
            [apoya.util.log :as log]
            [apoya.util.angular :refer [oset!]]
            [cljs.core.async :refer [<! take!]]))

(defn list-base-mails [$scope]
  (go
    (let [{:keys [baseLimit baseOffset]} $scope 
          base-mails (:body (<! (command/list-base-mails baseLimit baseOffset)))]
      (log/info base-mails))))

(defcontroller app CommandMailCtrl [$scope]
  (oset! $scope
         :section "mail")
  (list-base-mails $scope))

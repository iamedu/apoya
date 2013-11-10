(ns apoya.controllers.main
  (:require-macros [apoya.angular :refer [defcontroller]]
                   [cljs.core.async.macros :refer [go]])
  (:require [apoya.main :refer [app]]
            [apoya.services.auth :as auth]
            [apoya.util.log :as log]
            [cljs.core.async :refer [<!]]))

(defcontroller app MainCtrl [$scope]
  (go
    (let [{:keys [outcome body]} (<! (auth/find-current-user))]
      (log/info outcome)
      (.done js/NProgress))))

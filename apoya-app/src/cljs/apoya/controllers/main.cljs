(ns apoya.controllers.main
  (:require-macros [apoya.angular :refer [defcontroller]]
                   [cljs.core.async.macros :refer [go]])
  (:require [apoya.main :refer [app]]
            [apoya.services.auth :as auth]
            [apoya.util.log :as log]
            [apoya.util.angular :refer [oset!]]
            [cljs.core.async :refer [<!]]))

(defn load-main [$scope]
  (go
    (let [{:keys [outcome body]} (<! (auth/find-current-user))]
      (if (= :ok outcome)
        (oset! $scope :unknownUser (nil? body)))
      (.done js/NProgress))))

(defcontroller app MainCtrl [$scope]
  (load-main $scope))


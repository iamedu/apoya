(ns apoya.controllers.login
  (:require-macros [apoya.angular :refer [defcontroller]]
                   [cljs.core.async.macros :refer [go]])
  (:require [apoya.main :refer [app]]
            [apoya.topics :as t]
            [apoya.util.log :as log]
            [apoya.util.angular :refer [oset! keykeys]]
            [apoya.services.auth :as auth]
            [cljs.core.async :refer [<!]]))

(defn login [$scope $location user]
  (go
    (let [user (keykeys user)
          {:keys [outcome body]} (<! (auth/login user))]
      (when (= outcome :ok)
        (when-not (nil? body)
          (t/publish :identity body))
        (when (nil? body)
          (log/info "User or password incorrect")))
      (oset! $scope
             :tryingLogin false))))

(defcontroller app LoginCtrl [$scope $location]
  (oset! $scope
         :login (partial login $scope $location)))


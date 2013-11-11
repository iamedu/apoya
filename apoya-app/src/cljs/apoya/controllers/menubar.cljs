(ns apoya.controllers.menubar
  (:require-macros [apoya.angular :refer [defcontroller]]
                   [cljs.core.async.macros :refer [go]])
  (:require [apoya.main :refer [app]]
            [apoya.topics :as t]
            [apoya.services.auth :as auth]
            [apoya.util.log :as log]
            [apoya.util.angular :refer [oset!]]
            [cljs.core.async :refer [<!]]
            [clavatar-js.core :refer [gravatar]]))

(def initialized (atom false))

(defn handle-identity [$scope id]
  (let [{:keys [current authentications]} id
        {:keys [email]} (get authentications current)]
    (oset! $scope
           :gravatar (if email
                       (gravatar email :size 45))
           :identity (clj->js id))))

(defn handle-logout [$scope]
  (go
    (<! (auth/logout))))

(defcontroller app MenuBarCtrl [$scope $location]
  (.dropdown (js/jQuery ".dropdown-toggle"))
  (when-not @initialized
    (t/subscribe :identity (partial handle-identity $scope))
    (reset! initialized true))
  (handle-identity $scope @auth/user)
  (oset! $scope
         :location (.path $location)
         :logout (partial handle-logout $scope)))


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
                       (gravatar email :rating :g :default :identicon))
           :identity (clj->js id))))

(defn logout [$scope]
  (go
    (<! (auth/logout))
    (t/publish :logout true)))

(defcontroller app MenuBarCtrl [$scope $location]
  (.dropdown (js/jQuery ".dropdown-toggle"))
  (when-not @initialized
    (t/subscribe :identity (partial handle-identity $scope))
    (reset! initialized true))
  (handle-identity $scope @auth/user)

  (let [not-empty? (complement empty?)
        location-parts (-> (.path $location) (.split "/"))
        location-parts (filter not-empty? location-parts)
        location (first location-parts)]
    (oset! $scope
           :location location
           :logout (partial logout $scope))))


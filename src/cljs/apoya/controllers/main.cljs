(ns apoya.controllers.main
  (:require-macros [apoya.angular :refer [defcontroller]]
                   [cljs.core.async.macros :refer [go]])
  (:require [apoya.main :refer [app]]
            [apoya.services.auth :as auth]
            [apoya.topics :as t]
            [apoya.util.log :as log]
            [apoya.util.angular :refer [oset!]]
            [cljs.core.async :refer [<!]]))

(def initialized (atom false))

(defn handle-identity [$scope user]
  (oset! $scope
         :unknownUser (nil? user)))

(defcontroller app MainCtrl [$scope]
  (oset! $scope
         :unknownUser (nil? @auth/user)) 
  (when-not @initialized
    (t/subscribe :identity (partial handle-identity $scope)) 
    (reset! initialized true)))


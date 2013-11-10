(ns apoya.controllers.menubar
  (:require-macros [apoya.angular :refer [defcontroller]]
                   [cljs.core.async.macros :refer [go]])
  (:require [apoya.main :refer [app]]
            [apoya.topics :as t]
            [apoya.services.auth :as auth]
            [apoya.util.log :as log]
            [apoya.util.angular :refer [oset!]]
            [cljs.core.async :refer [<!]]))

(defcontroller app MenuBarCtrl [$scope $location]
  (t/subscribe :identity
               (fn [id]
                 (log/info id)))
  (oset! $scope
         :location (.path $location)))

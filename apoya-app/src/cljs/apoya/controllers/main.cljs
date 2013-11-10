(ns apoya.controllers.main
  (:require-macros [apoya.angular :refer [defcontroller]]
                   [cljs.core.async.macros :refer [go]])
  (:require [apoya.main :refer [app]]
            [apoya.services.auth :as auth]
            [apoya.util.log :as log]
            [apoya.util.angular :refer [oset!]]
            [cljs.core.async :refer [<!]]))

(defcontroller app MainCtrl [$scope]
  (oset! $scope
         :unknownUser (nil? @auth/user)))


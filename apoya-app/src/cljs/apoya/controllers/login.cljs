(ns apoya.controllers.login
  (:require-macros [apoya.angular :refer [defcontroller]])
  (:require [apoya.main :refer [app]]
            [apoya.util.angular :refer [oset!]]))

(defcontroller app LoginCtrl [$scope]
  (oset! $scope))


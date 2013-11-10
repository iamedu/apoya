(ns apoya.controllers.main
  (:require-macros [apoya.angular :refer [defcontroller]])
  (:require [apoya.main :refer [app]]
            [apoya.util.log :as log]))

(defcontroller app MainCtrl [$scope])

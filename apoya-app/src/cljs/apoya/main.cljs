(ns apoya.main
  (:require-macros [apoya.angular :refer [defcontroller]])
  (:require [apoya.util.log :as log]))

(def *default-output* (log/console-output))
(log/start-display *default-output*)

(def app (.module js/angular "apoyaApp" (array "ngRoute")))

(defn config-app [$routeProvider $httpProvider]
  (doto $routeProvider
    (.when "/" (clj->js {:templateUrl "views/main.html"
                         :controller :MainCtrl}))))

(-> app
    (.config (array "$routeProvider" "$httpProvider" config-app)))


(defn app-started []
  (log/info "Resources have been loaded"))

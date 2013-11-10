(ns apoya.main
  (:require-macros [apoya.angular :refer [defcontroller]])
  (:require [apoya.util.log :as log]
            [apoya.topics :as t]
            [apoya.services.auth :as auth]))

(def *default-output* (log/console-output))
(log/start-display *default-output*)

(def app (.module js/angular "apoyaApp" (array "ngRoute")))

(defn config-app [$routeProvider $httpProvider]
  (doto $routeProvider
    (.when "/" (clj->js {:templateUrl "views/main.html"
                         :controller :MainCtrl}))
    (.when "/login" (clj->js {:templateUrl "views/login.html"
                              :controller :LoginCtrl}))
    (.when "/signup" (clj->js {:templateUrl "views/signup.html"}))))

(defn run-app [$location $rootScope]
  (auth/check-user))

(-> app
    (.config (array "$routeProvider" "$httpProvider" config-app))
    (.run (array "$location" "$rootScope" run-app)))

(defn app-started []
  (log/info "Resources have been loaded"))


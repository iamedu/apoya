(ns apoya.main
  (:require-macros [apoya.angular :refer [defcontroller]])
  (:require [apoya.util.log :as log]
            [apoya.util.angular :refer [oset!]]
            [apoya.topics :as t]
            [apoya.services.auth :as auth]
            [goog.string :as gstring]))

(def *default-output* (log/console-output))
(log/start-display *default-output*)

(def app (.module js/angular "apoyaApp" (array "ngRoute")))

(defn config-app [$routeProvider $httpProvider]
  (doto $routeProvider
    (.when "/" (clj->js {:templateUrl "views/main.html"
                         :controller :MainCtrl}))
    (.when "/error/:errorId" (clj->js {:templateUrl "views/error.html"
                              :controller :ErrorCtrl}))
    (.when "/login" (clj->js {:templateUrl "views/login.html"
                              :controller :LoginCtrl}))
    (.when "/signup" (clj->js {:templateUrl "views/signup.html"}))
    (.when "/dashboard" (clj->js {:templateUrl "views/dashboard.html"}))))

(defn handle-error [$location $rootScope {:keys [outcome status body]}]
  (let [{error-id :error-id} body
        current-location (.path $location)]
    (when-not (gstring/startsWith current-location "/error")
      (.path $location (str "/error/" error-id))
      (.$apply $rootScope))))

(defn handle-identity [$location $rootScope id]
  (when (nil? @auth/user)
    (.path $location "/dashboard")
    (.$apply $rootScope))
  (reset! auth/user id))

(defn run-app [$location $rootScope]
  (t/subscribe :error (partial handle-error $location $rootScope))
  (t/subscribe :ready (fn [_] (oset! $rootScope :ready true)))
  (t/subscribe :identity (partial handle-identity $location $rootScope))
  (auth/check-user))

(-> app
    (.config (array "$routeProvider" "$httpProvider" config-app))
    (.run (array "$location" "$rootScope" run-app)))

(defn app-started []
  (log/info "Resources have been loaded"))


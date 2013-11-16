(ns apoya.main
  (:require [apoya.util.log :as log]
            [apoya.util.angular :refer [oset!]]
            [apoya.topics :as t]
            [apoya.services.auth :as auth]
            [goog.string :as gstring]))

(def *default-output* (log/console-output))
(log/start-display *default-output*)

;; Global modals
(def roles-opened (atom false))

(def app (.module js/angular "apoyaApp" (array "ngRoute" "ui.bootstrap" "ui.codemirror" "infinite-scroll")))
(def public-urls #{"/", "/login", "/signup", "/error"})

(defn config-app [$routeProvider $httpProvider]
  (doto $routeProvider
    (.when "/" (clj->js {:templateUrl "views/main.html"
                         :controller :MainCtrl}))
    (.when "/error/:errorId" (clj->js {:templateUrl "views/error.html"
                                       :controller :ErrorCtrl}))
    (.when "/login" (clj->js {:templateUrl "views/login.html"
                              :controller :LoginCtrl}))
    (.when "/signup" (clj->js {:templateUrl "views/signup.html"}))
    (.when "/dashboard" (clj->js {:templateUrl "views/dashboard.html"}))
    (.when "/command/scripting" (clj->js {:templateUrl "views/command/scripting.html"
                                    :controller :CommandScriptingCtrl}))
    (.when "/command/repl" (clj->js {:templateUrl "views/command/repl.html"
                                    :controller :CommandReplCtrl}))
    (.when "/command/sql" (clj->js {:templateUrl "views/command/sql.html"
                                    :controller :CommandSqlCtrl}))
    (.when "/command/:section" (clj->js {:templateUrl "views/command.html"
                                         :controller :CommandCtrl}))
    (.otherwise (clj->js {:templateUrl "views/otherwise.html"}))))

(defn handle-forbidden [m]
  (log/info (str "Forbidden request" m)))

(defn handle-error [$location $rootScope {:keys [outcome status body] :as m}]
  (let [{error-id :error-id} body
        current-location (.path $location)]
    (if (not= status 403)
      (when-not (gstring/startsWith current-location "/error")
        (log/info "Ocurrio un error")
        (.path $location (str "/error/" error-id))
        (.$apply $rootScope))
      (handle-forbidden m))))

(defn handle-identity [$location $rootScope $modal id]
  (let [was-nil? (nil? @auth/user)
        location (.path $location)]
    (reset! auth/user id) 
    (when (and was-nil?
               (public-urls location))
      (.path $location "/dashboard")
      (.$apply $rootScope))))

(defn handle-logout [$location $rootScope _]
  (reset! auth/user nil)
  (reset! auth/role nil)
  (when-not (or (public-urls (.path $location))
                (gstring/startsWith (.path $location) "/error"))
    (.path $location "/")
    (.$apply $rootScope)))

(defn finished-loading [$location $rootScope]
  (let [location (.path $location)]
    (when (and (nil? @auth/user)
               (not (public-urls location))
               (not (gstring/startsWith location "/error")))
      (.path $location "/")
      (.$apply $rootScope)))
  (.done js/NProgress))

(defn run-app [$location $rootScope $modal]
  (t/subscribe :error (partial handle-error $location $rootScope))
  (t/subscribe :ready (fn [_] (oset! $rootScope :ready true)))
  (t/subscribe :identity (partial handle-identity $location $rootScope $modal))
  (t/subscribe :logout (partial handle-logout $location $rootScope))
  (.$on $rootScope "$locationChangeStart"
        (fn [event next-location current-location]
          (let [reg #"https?://[\w\.:\-]+/#(/[\w\.\-\#\/]*)"
                [[_ location]] (re-seq reg next-location)]
            (if (and @auth/started
                     (nil? @auth/user)
                     (not (public-urls location)))
              (.preventDefault event)))))
  (auth/check-user (partial finished-loading $location $rootScope)))

(-> app
    (.config (array "$routeProvider" "$httpProvider" config-app))
    (.run (array "$location" "$rootScope" "$modal" run-app)))

(defn app-started []
  (log/info "Resources have been loaded"))


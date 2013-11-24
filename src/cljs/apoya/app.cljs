(ns apoya.main
  (:require [apoya.util.log :as log]
            [apoya.util.angular :refer [oset!]]
            [apoya.topics :as t]
            [apoya.services.auth :as auth]
            [goog.string :as gstring]))

(def *default-output* (log/console-output))
(def location-history (atom (list)))
(log/start-display *default-output*)

;; Global modals
(def roles-opened (atom false))

(def app (.module js/angular "apoyaApp" (array "ngRoute" "ui.bootstrap" "ui.codemirror" "infinite-scroll")))
(def public-urls #{"/", "/login", "/signup", "/error"})

(defn config-app [$routeProvider $httpProvider]
  (doto $routeProvider
    (.when "/" (clj->js {:templateUrl "views/main.html"
                         :controller :MainCtrl}))
    (.when "/error404" (clj->js {:templateUrl "views/error404.html"
                                 :controller :Error404Ctrl}))
    (.when "/error403" (clj->js {:templateUrl "views/error403.html"
                                 :controller :Error403Ctrl}))
    (.when "/error/:errorId" (clj->js {:templateUrl "views/error.html"
                                       :controller :ErrorCtrl}))
    (.when "/login" (clj->js {:templateUrl "views/login.html"
                              :controller :LoginCtrl}))
    (.when "/signup" (clj->js {:templateUrl "views/signup.html"}))
    (.when "/dashboard" (clj->js {:templateUrl "views/dashboard.html"}))
    (.when "/command/main" (clj->js {:templateUrl "views/command/main.html"
                                     :controller :CommandMainCtrl}))
    (.when "/command/app" (clj->js {:templateUrl "views/command/app.html"
                                    :controller :CommandAppCtrl}))
    (.when "/command/scripting" (clj->js {:templateUrl "views/command/scripting.html"
                                          :controller :CommandScriptingCtrl}))
    (.when "/command/filesystem" (clj->js {:templateUrl "views/command/filesystem.html"
                                           :controller :CommandFilesystemCtrl}))
    (.when "/command/repl" (clj->js {:templateUrl "views/command/repl.html"
                                     :controller :CommandReplCtrl}))
    (.when "/command/sql" (clj->js {:templateUrl "views/command/sql.html"
                                    :controller :CommandSqlCtrl}))
    (.when "/command/:section" (clj->js {:templateUrl "views/command.html"
                                         :controller :CommandCtrl}))
    (.otherwise (clj->js {:templateUrl "views/otherwise.html"}))))

(defn handle-forbidden [m]
  (log/info (str "Forbidden request" m)))

(defn handle-not-found [m]
  (log/info (str "Not found request" m)))

(defn handle-error [$location $rootScope {:keys [status body route] :as m}]
  (let [{:keys [error-id]} body
        current-location (.path $location)
        [redirect-location redirect-search] (condp = status
                                              500 [(str "/error/" error-id) {}]
                                              [(str "/error" status) route])]
    (when-not (gstring/startsWith current-location "/error")
      (log/info (str "Ocurrio un error " status))
      (.path $location redirect-location)
      (if (seq route)
        (.search $location (clj->js route)))
      (.$apply $rootScope))))

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
    (.$apply $rootScope)
    (.reload js/location)))

(defn finished-loading [$location $rootScope]
  (let [location (.path $location)]
    (when (and (nil? @auth/user)
               (not (public-urls location))
               (not (gstring/startsWith location "/error")))
      (.path $location "/")
      (.$apply $rootScope))))

(defn supplant-user [$rootScope $modal]
  (.open $modal
         (clj->js {:templateUrl "views/modals/changeUser.html"
                   :controller :SupplantModalCtrl})))

(defn change-site [$rootScope $modal]
  (.open $modal
         (clj->js {:templateUrl "views/modals/changeSite.html"
                   :controller :ChangeSiteCtrl})))

(defn setup-cache [$templateCache]
  (.put $templateCache
        "template/messageBox/message.html",
        "<div class='modal-dialog'>
         <div class='modal-content'>
         <div class='modal-header'><h3>{{title}}</h3></div>
         <div class='modal-body'><p>{{message}}</p></div>
         <div class='modal-footer'><button ng-repeat='btn in buttons' ng-click='close(btn.result)' class='btn' ng-class='btn.cssClass'>{{btn.label}}</button></div>
         </div>
         </div>"))

(defn run-app [$location $rootScope $modal $templateCache]
  (setup-cache $templateCache)
  (t/subscribe :error (partial handle-error $location $rootScope))
  (t/subscribe :ready (fn [_] (oset! $rootScope :ready true)))
  (t/subscribe :identity (partial handle-identity $location $rootScope $modal))
  (t/subscribe :logout (partial handle-logout $location $rootScope))
  (t/subscribe :supplant-user (partial supplant-user $rootScope $modal))
  (t/subscribe :change-site (partial change-site $rootScope $modal))
  (.$on $rootScope "$locationChangeStart"
        (fn [event next-location current-location]
          (let [reg #"https?://[\w\.:\-]+/#(/[\w\.\-\#\/]*)"
                [[_ location]] (re-seq reg next-location)]
            (if (and @auth/started
                     (nil? @auth/user)
                     (not (public-urls location)))
              (.preventDefault event)
              (swap! location-history conj current-location)))))
  (auth/check-user (partial finished-loading $location $rootScope)))

(-> app
    (.config (array "$routeProvider" "$httpProvider" config-app))
    (.run (array "$location" "$rootScope" "$modal" "$templateCache" run-app)))

(defn app-started []
  (log/info "Resources have been loaded"))


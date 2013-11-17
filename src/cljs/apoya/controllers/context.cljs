(ns apoya.controllers.context
  (:require-macros [apoya.angular :refer [defcontroller]]
                   [cljs.core.async.macros :refer [go]])
  (:require [apoya.main :refer [app]]
            [apoya.topics :as t]
            [apoya.util.log :as log]
            [apoya.util.angular :refer [oset! keykeys]]
            [apoya.services.auth :as auth]
            [apoya.services.site :as site]
            [cljs.core.async :refer [<!]]))

(def context-changes (atom #{}))

(defn check-context [ctx]
  (some #{ctx} @context-changes))

(defn handle-context-change [$scope]
  (let [main-container (js/jQuery "#mainContainer")
        site-navbar (js/jQuery "#siteChangedNavbar")
        main-offset (* 50 (count @context-changes))
        site-offset (if (= (count @context-changes) 2)
                      50
                      0)]
    (oset! $scope
           :userSupplanted (check-context :impersonate)
           :siteChanged (check-context "site"))
    (.css main-container "margin-top" (str main-offset "px"))
    (.css site-navbar "margin-top" (str site-offset "px"))))

(defn check-changes [$scope]
  (go
    (let [changes (:body (<! (site/list-context-changes)))
          change-keys (keys changes)
          {:keys [impersonate site]} changes]
      (reset! context-changes change-keys)
      (when impersonate
        (oset! $scope :currentUser (:current impersonate)))
      (when site
        (oset! $scope :currentSite (:current site)))
      (handle-context-change $scope))))

(defn end-impersonation [$scope $location]
  (go
    (when (:body (<! (auth/end-impersonation)))
      (.path $location "/dashboard")
      (.$apply $scope)
      (.reload js/location))))

(defcontroller app ContextCtrl [$scope $location]
  (oset! $scope
         :endImpersonation (partial end-impersonation $scope $location))
  (check-changes $scope))



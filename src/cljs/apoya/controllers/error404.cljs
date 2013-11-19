(ns apoya.controllers.error404
  (:require-macros [apoya.angular :refer [defcontroller]]
                   [cljs.core.async.macros :refer [go]])
  (:require [apoya.main :refer [app]]
            [apoya.topics :as t]
            [apoya.services.auth :as auth]
            [apoya.services.error :as error]
            [apoya.util.log :as log]
            [apoya.util.angular :refer [oset!]]
            [cljs.core.async :refer [<!]]))

(defcontroller app Error404Ctrl [$scope $routeParams]
  (go
    (let [{:keys [method uri]} $routeParams
          permissions (:body (<! (auth/has-permissions ["admin:error"])))
          is-admin (boolean (get permissions "admin:error"))]
      (oset! $scope
             :isAdmin is-admin
             :method method
             :uri uri
             :back #(.back js/history)))))

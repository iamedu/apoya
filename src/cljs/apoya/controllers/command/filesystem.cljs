(ns apoya.controllers.command.filesystem
  (:require-macros [apoya.angular :refer [defcontroller]]
                   [cljs.core.async.macros :refer [go]])
  (:require [apoya.main :refer [app]]
            [apoya.services.auth :as auth]
            [apoya.services.command :as command]
            [apoya.util.log :as log]
            [apoya.util.angular :refer [oset!]]
            [cljs.core.async :refer [<!]]))

(defcontroller app CommandFilesystemCtrl [$scope $routeParams]
  (let [{:keys [path] :or {path "/"}} $routeParams]
    (oset! $scope
           :section "filesystem")))


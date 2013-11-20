(ns apoya.controllers.command.filesystem
  (:require-macros [apoya.angular :refer [defcontroller]]
                   [cljs.core.async.macros :refer [go]])
  (:require [apoya.main :refer [app]]
            [apoya.services.auth :as auth]
            [apoya.services.command :as command]
            [apoya.services.fs :as fs]
            [apoya.util.log :as log]
            [apoya.util.angular :refer [oset!]]
            [cljs.core.async :refer [<!]]))

(defn load-files [$scope path]
  (go
    (let [files (->> (<! (fs/list-contents path))
                     :body
                     (sort-by :type >))]
      (oset! $scope
             :files files))))

(defcontroller app CommandFilesystemCtrl [$scope $routeParams]
  (let [{:keys [path] :or {path ""}} $routeParams]
    (oset! $scope
           :loadFiles (partial load-files $scope)
           :section "filesystem")
    (load-files $scope path)))


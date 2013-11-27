(ns apoya.controllers.command.filesystem
  (:require-macros [apoya.angular :refer [defcontroller]]
                   [cljs.core.async.macros :refer [go]])
  (:require [apoya.main :refer [app]]
            [apoya.services.auth :as auth]
            [apoya.services.command :as command]
            [apoya.services.fs :as fs]
            [apoya.util.log :as log]
            [apoya.util.angular :refer [oset!]]
            [clojure.string :as cstr]
            [cljs.core.async :refer [<!]]))

(defn load-files [$scope path]
  (go
    (let [files (->> (<! (fs/list-contents path))
                     :body
                     (sort-by :type >)
                     distinct)]
      (oset! $scope
             :files files))))

(defn backspace-shortcut [$scope path]
  (log/info path)
  (js/key "backspace" #(if-not (empty? path)
                        (.back js/history))))

(defcontroller app CommandFilesystemCtrl [$scope $routeParams $location $rootScope]
  ;FileSystem finder
  (js/key "t" (fn []
                (-> $location (.path "/command/filesystem/find") (.search (clj->js {})))
                (.$apply $rootScope)))

  (let [{:keys [path] :or {path ""}} $routeParams]
    (oset! $scope
           :path path
           :loadFiles (partial load-files $scope)
           :section "filesystem")
    (load-files $scope path)
    (backspace-shortcut $scope path)))


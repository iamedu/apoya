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
  (js/key "backspace" #(if-not (empty? path)
                        (.back js/history))))

(defn load-breadcumb [$scope path]
  (let [directories (concat '("Home") (rest (cstr/split path #"/")))]
    (oset! $scope
           :directories directories)))

(defn breadcrumb-click [$location path name]
  (let [search (if (= name "Home")
                 nil
                 (let [directories (cstr/split path #"/")]
                   (str "/"
                        (cstr/join "/"
                                   (loop [dirs (next directories)]
                                     (cond
                                       (zero? (count dirs)) '()
                                       (= name (last dirs))  dirs
                                       :else (recur (drop-last dirs))))))))]

    (-> $location
        (.path "/command/filesystem")
        (.search (clj->js (if (nil? search) {} {:path search}) ))))
  )

(defcontroller app CommandFilesystemCtrl [$scope $routeParams $location $rootScope]
  ;FileSystem finder
  (js/key "t" (fn []
                (-> $location (.path "/command/filesystem/find") (.search (clj->js {})))
                (.$apply $rootScope)))

  (let [{:keys [path] :or {path ""}} $routeParams]
    (oset! $scope
           :path path
           :loadFiles (partial load-files $scope)
           :breadcrumbClick (partial breadcrumb-click $location path)
           :section "filesystem")
    (load-files $scope path)
    (load-breadcumb $scope path)
    (backspace-shortcut $scope path)))


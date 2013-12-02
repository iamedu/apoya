(ns apoya.controllers.command.main
  (:require-macros [apoya.angular :refer [defcontroller]]
                   [cljs.core.async.macros :refer [go]])
  (:require [apoya.main :refer [app]]
            [apoya.services.auth :as auth]
            [apoya.services.command :as command]
            [apoya.util.log :as log]
            [apoya.util.angular :refer [oset!]]
            [cljs.core.async :refer [<! take!]]))

(defcontroller app CommandMainCtrl [$scope $sce]
  (go
    (let [{:keys [changelog clojure-version] :as platform-meta} (:body (<! (command/platform-meta)))
          changelog (.trustAsHtml $sce changelog)]
      (oset! $scope
             :clojureVersion clojure-version
             :platformMeta platform-meta
             :changelog changelog)))
  (oset! $scope :section "main"))


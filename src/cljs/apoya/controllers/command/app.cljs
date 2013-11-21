(ns apoya.controllers.command.app
  (:require-macros [apoya.angular :refer [defcontroller]]
                   [cljs.core.async.macros :refer [go]])
  (:require [apoya.main :refer [app]]
            [apoya.services.auth :as auth]
            [apoya.services.command :as command]
            [apoya.util.log :as log]
            [apoya.util.angular :refer [oset!]]
            [cljs.core.async :refer [<! take!]]))

(defcontroller app CommandAppCtrl [$scope $sce]
  (go
    (let [{changelog :changelog :as app-meta} (:body (<! (command/app-meta)))
          changelog (.trustAsHtml $sce changelog)]
      (oset! $scope
             :appMeta app-meta
             :changelog changelog)))
  (oset! $scope :section "app"))



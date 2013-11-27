(ns apoya.controllers.command.finder
  (:require-macros [apoya.angular :refer [defcontroller]]
                   [cljs.core.async.macros :refer [go]])
  (:require [apoya.main :refer [app]]
            [apoya.services.auth :as auth]
            [apoya.services.command :as command]
            [apoya.services.fs :as fs]
            [apoya.util.log :as log]
            [apoya.util.angular :refer [oset!]]
            [cljs.core.async :refer [<!]]))

(defcontroller app CommandFinderCtrl [$scope $routeParams $location $rootScope]
  (log/info "Finder")
  (js/key "esc" (fn []
                (.back js/history)))  )

(ns apoya.controllers.error403
  (:require-macros [apoya.angular :refer [defcontroller]]
                   [cljs.core.async.macros :refer [go]])
  (:require [apoya.main :refer [app]]
            [apoya.topics :as t]
            [apoya.services.auth :as auth]
            [apoya.services.error :as error]
            [apoya.util.log :as log]
            [apoya.util.angular :refer [oset!]]
            [cljs.core.async :refer [<!]]))

(defcontroller app Error403Ctrl [$scope $routeParams])

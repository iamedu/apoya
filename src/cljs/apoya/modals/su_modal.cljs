(ns apoya.modals.su-modal
  (:require-macros [apoya.angular :refer [defcontroller]]
                   [cljs.core.async.macros :refer [go]])
  (:require [apoya.util.log :as log]
            [apoya.main :refer [app]]
            [apoya.util.angular :refer [oset!]]
            [apoya.topics :as t]
            [apoya.services.auth :as auth]
            [cljs.core.async :refer [<!]]))

(defcontroller app SupplantModalCtrl [$scope $modalInstance]
  (oset! $scope))

(ns apoya.modals.ch-site-modal
  (:require-macros [apoya.angular :refer [defcontroller]]
                   [cljs.core.async.macros :refer [go]])
  (:require [apoya.util.log :as log]
            [apoya.main :refer [app]]
            [apoya.util.angular :refer [oset!]]
            [apoya.topics :as t]
            [apoya.services.auth :as auth]
            [apoya.services.site :as site]
            [cljs.core.async :refer [<!]]))

(defn change-site [$modalInstance domain]
  (go
    (when (:body (<! (site/change-site domain)))
      (.close $modalInstance)
      (.reload js/location))))

(defcontroller app ChangeSiteCtrl [$scope $modalInstance $location]
  (go
    (let [sites (:body (<! (site/list-sites)))]
      (oset! $scope
             :changeSite (partial change-site $modalInstance)
             :sites sites))))


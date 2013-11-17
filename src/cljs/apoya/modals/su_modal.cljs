(ns apoya.modals.su-modal
  (:require-macros [apoya.angular :refer [defcontroller]]
                   [cljs.core.async.macros :refer [go]])
  (:require [apoya.util.log :as log]
            [apoya.main :refer [app]]
            [apoya.util.angular :refer [oset!]]
            [apoya.topics :as t]
            [apoya.services.auth :as auth]
            [cljs.core.async :refer [<!]]))

(defn impersonate [$scope $modalInstance $location username]
  (go
    (when (<! (auth/impersonate username))
      (.close $modalInstance)
      (.path $location "/dashboard")
      (.$apply $scope)
      (.reload js/location))))

(defn check-exists [$scope]
  (go
    (let [user (get-in $scope [:user :username])
          exists? (:body (<! (auth/can-impersonate? user)))]
      (oset! $scope :exists exists?))))

(defcontroller app SupplantModalCtrl [$scope $modalInstance $location]
  (.$watch $scope "user.username" (partial check-exists $scope))
  (oset! $scope
         :user {}
         :impersonate (partial impersonate $scope $modalInstance $location)))

(ns apoya.modals.role-modal
  (:require-macros [apoya.angular :refer [defcontroller]])
  (:require [apoya.util.log :as log]
            [apoya.main :refer [app]]
            [apoya.util.angular :refer [oset!]]
            [apoya.topics :as t]
            [apoya.services.auth :as auth]))

(defcontroller app RoleModalCtrl [$scope $modalInstance])

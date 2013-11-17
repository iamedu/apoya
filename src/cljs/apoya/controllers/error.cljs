(ns apoya.controllers.error
  (:require-macros [apoya.angular :refer [defcontroller]]
                   [cljs.core.async.macros :refer [go]])
  (:require [apoya.main :refer [app]]
            [apoya.topics :as t]
            [apoya.services.auth :as auth]
            [apoya.services.error :as error]
            [apoya.util.log :as log]
            [apoya.util.angular :refer [oset!]]
            [cljs.core.async :refer [<!]]))

(defn handle-admin-error [$scope error-id]
  (go
    (let [{metadata :metadata :as error-details} (:body (<! (error/find-error error-id)))
          full-metadata (->> metadata (.parse js/JSON) (js->clj))
          metadata (dissoc full-metadata "params" "headers" "cookies" "body-params"
                           "multipart-params" "query-params")]
      (log/info (keys metadata))
      (oset! $scope
             :metadata metadata
             :fullMetadata full-metadata
             :errorDetails error-details))))

(defcontroller app ErrorCtrl [$scope $routeParams]
  (go
    (let [{error-id "errorId"} $routeParams
          permissions (:body (<! (auth/has-permissions ["admin:error"])))
          is-admin (boolean (get permissions "admin:error"))]
      (oset! $scope
             :isAdmin is-admin
             :back #(.back js/history)
             :errorId error-id)
      (if is-admin (handle-admin-error $scope error-id)))))


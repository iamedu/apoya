(ns apoya.controllers.command.app
  (:require-macros [apoya.angular :refer [defcontroller]]
                   [cljs.core.async.macros :refer [go]])
  (:require [apoya.main :refer [app]]
            [apoya.services.dialog :as dialog]
            [apoya.services.auth :as auth]
            [apoya.services.command :as command]
            [apoya.services.upload :as upload]
            [apoya.util.log :as log]
            [apoya.util.angular :refer [oset!]]
            [goog.string :as gstr]
            [cljs.core.async :refer [<! take!]]))

(defn list-files [$scope]
  (go
    (let [files (:body (<! (command/list-apps)))]
      (oset! $scope :appVersions files))))

(defn file-upload-progress [$scope {:keys [loaded total]}]
  (oset! $scope :uploadPercent (* 100 (/ loaded total))))

(defn file-upload-complete [$scope evt xhr]
  (oset! $scope :uploadPercent 100)
  (list-files $scope))

(defn show-invalid-type [$dialog]
  (.messageBox $dialog
               "Invalid file type"
               "This file type is not supported, you must upload a far file"
               [{:label "Ok"}]))

(defn file-uploaded [$scope $dialog evt]
  (oset! $scope :uploadPercent 0)
  (let [files (upload/event-files evt)
        far? (->> (map :name files)
                  (some #(gstr/endsWith % ".far"))
                  (boolean))]
    (if far?
      (upload/upload-files "/api/v1/command/app/upload.edn" files
                           :progress (partial file-upload-progress $scope)
                           :complete (partial file-upload-complete $scope))
      (show-invalid-type $dialog))))

(defn show-app-details [$scope $modal path]
  (go
    (if-let [app-details (:body (<! (command/show-app-detail path)))]
      (.open $modal (clj->js {:templateUrl "views/modals/appDetails.html"
                              :controller :CommandAppDetailCtrl
                              :resolve {:details #(identity app-details)}})))))

(defn deploy [$scope $modalInstance path]
  (go
    (when-let [deployed (:body (<! (command/deploy path)))]
      (.close $modalInstance)
      (.reload js/location))))

(defcontroller app CommandAppCtrl [$scope $sce $modal $dialog]
  (upload/watch-uploads "appFileUpload" :on-change (partial file-uploaded $scope $dialog))
  (go
    (let [{changelog :changelog :as app-meta} (:body (<! (command/app-meta)))
          changelog (.trustAsHtml $sce changelog)]
      (oset! $scope
             :appMeta app-meta
             :changelog changelog
             :showAppDetails (partial show-app-details $scope $modal))))
  (oset! $scope :section "app")
  (list-files $scope))

(defcontroller app CommandAppDetailCtrl [$scope $sce $modalInstance details]
  (let [{:keys [app-name changelog commit version path]} details]
    (oset! $scope
           :path path
           :deploy (partial deploy $scope $modalInstance)
           :changelog (.trustAsHtml $sce changelog)
           :name app-name
           :commit commit
           :version version)))


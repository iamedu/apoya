(ns apoya.services.dialog
  (:require-macros [apoya.angular :refer [defservice defcontroller]])
  (:require [apoya.main :refer [app]]
            [apoya.util.log :as log]
            [apoya.util.angular :refer [oset!]]))

(defn dialog-fn [$modal {:keys [template-url controller scope]} result-fn]
  (let [modal-opts (doto (clj->js {:templateUrl template-url
                                   :controller controller})
                     (aset "scope" scope))
        dialog (.open $modal modal-opts)]
    (if result-fn (-> dialog (.-result) (.then result-fn)))
    (aset dialog "values" modal-opts)
    dialog))

(defn build-scope [$rootScope title message buttons]
  (let [new-scope (.$new $rootScope)]
    (.extend js/angular new-scope
             (clj->js {:title title
                       :message message
                       :buttons buttons}))))

(defn message-box [$rootScope $modal title message buttons result-fn]
  (let [scope (build-scope $rootScope title message buttons)
        handle-result (fn [result]
                        (let [value (if result-fn (result-fn result))]
                          (.$destroy scope)
                          value))]
    (dialog-fn $modal
               {:template-url "template/messageBox/message.html"
                :controller "MessageBoxController"
                :scope scope}
               handle-result)))

(defservice app $dialog [$rootScope $modal]
  (let [funcs {:dialog (partial dialog-fn $modal)
               :simpleDialog (fn [template-url controller result-fn]
                               (dialog-fn $modal
                                          {:template-url template-url
                                           :controller controller}
                                          result-fn))
               :messageBox (partial message-box $rootScope $modal)}]
    (clj->js funcs)))


(defcontroller app MessageBoxController [$scope $modalInstance]
  (oset! $scope
         :close (fn [result]
                  (.close $modalInstance result))))

(ns apoya.controllers.command.scripting
  (:require-macros [apoya.angular :refer [defcontroller]]
                   [cljs.core.async.macros :refer [go]])
  (:require [apoya.main :refer [app]]
            [apoya.services.auth :as auth]
            [apoya.services.command :as command]
            [apoya.util.log :as log]
            [apoya.util.angular :refer [oset!]]
            [cljs.core.async :refer [<!]]))

(declare refresh-cm)

(defn load-cm [cm]
  (js/setTimeout #(.refresh cm) 200))

(def modes
  {"clojure" "text/x-clojure"
   "groovy" "text/x-groovy"
   "js" "text/x-js"})

(defn store-last-script [engine code]
  (.set js/store "last-script-code" code)
  (.set js/store "last-script-engine" engine))

(defn read-last-script []
  {:code (.get js/store "last-script-code")
   :engine (.get js/store "last-script-engine")})

(defn change-engine [$scope engine]
  (let [mode (get modes engine)
        scripting-options (js->clj (:scriptingOptions $scope))
        scripting-options (assoc scripting-options
                                 :mode mode)]
    (oset! $scope :scriptingOptions scripting-options)))

(defn execute [$scope engine code]
  (store-last-script engine code)
  (go
    (let [{:keys [exception output error-output]} (:body (<! (command/eval-code engine code)))
          section (cond 
                    (not-empty output) "output"
                    (not-empty error-output) "error-output"
                    (not-empty exception) "exception"
                    :else nil)]
      (oset! $scope
             :innerSection section
             :lastUpdated (js/Date.)
             :output output
             :errorOutput error-output
             :exception exception))))

(defcontroller app CommandScriptingCtrl [$scope]
  (let [{:keys [code engine]} (read-last-script)]
    (oset! $scope
           :scriptingOptions {:matchBrackets true
                              :autofocus true
                              :lineWrapping true
                              :lineNumbers true
                              :onLoad load-cm}
           :code code
           :selectedEngine engine
           :section "scripting"
           :execute (partial execute $scope)))
  (.$watch $scope "selectedEngine"
           (partial change-engine $scope))
  (go
    (let [engines (:body (<! (command/list-engines)))]
      (oset! $scope
             :scriptingEngines engines))))


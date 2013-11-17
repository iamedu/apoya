(ns apoya.services.site
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [apoya.remote.request :as r]))

(defn list-context-changes []
  (r/edn [:post "/api/public/v1/site/list-context-changes.edn"]))


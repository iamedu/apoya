(ns apoya.services.site
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [apoya.remote.request :as r]))

(defn list-context-changes []
  (r/edn [:post "/api/public/v1/site/list-context-changes.edn"]))

(defn list-sites []
  (r/edn [:post "/api/v1/site/list-sites.edn"]))

(defn change-site [domain]
  (r/edn [:post "/api/v1/site/change-site.edn"]
         :content {:domain domain}))

(defn end-change-site []
  (r/edn [:post "/api/public/v1/site/end-change-site.edn"]))

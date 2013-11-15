(ns apoya.services.command
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [apoya.remote.request :as r]))

;; SQL Sessions
(defn create-session []
  (r/edn [:post "/api/v1/command/sql/create-session.edn"]))

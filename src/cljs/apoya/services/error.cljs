(ns apoya.services.error
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [apoya.remote.request :as r]))

;; SQL Sessions
(defn find-error [error-id]
  (r/edn [:post "/api/v1/error/find-error.edn"]
         :content {:error-id error-id}))

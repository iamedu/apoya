(ns apoya.services.auth
  (:require [apoya.remote.request :as r]
            [apoya.util.log :as log]))

(defn find-current-user []
  (r/edn [:post "/api/public/v1/auth/identity.edn"]))

(ns apoya.routes.error
  (:use compojure.core)
  (:require [apoya.response :as r]
            [apoya.data.error :as err]
            [clojure.tools.logging :as log]))

;;; Sql
(defroutes error-routes
  (POST "/find-error.edn" [error-id]
        (let [error (err/find-error error-id)]
          (r/edn-response error))))

(ns apoya.routes
  (:use compojure.core)
  (:require [ring.util.response :as response]))

(defroutes app-routes
  (GET "/test" []
       (response/response "The test is working")))

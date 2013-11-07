(ns apoya.response
  (:require [ring.util.response :as response]))

(defn edn-response [body]
  (-> (response/response (pr-str body))
      (response/content-type "application/edn; charset=utf-8")))

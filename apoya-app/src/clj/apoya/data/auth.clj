(ns apoya.data.auth
  (:require [clojure.tools.logging :as log])
  (:use korma.core
        apoya.data.schema))

(defn find-user [& {:as criteria}]
  (first (select users
                 (where (merge criteria {:active true}))
                 (limit 1))))

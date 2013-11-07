(ns apoya.data.auth
  (:require [clojure.tools.logging :as log])
  (:use korma.core
        apoya.data.schema))

(defn find-user [username]
  (first (select users
                 (where {:username username
                         :active true})
                 (limit 1))))

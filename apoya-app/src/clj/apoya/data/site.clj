(ns apoya.data.site
  (:require [clojure.tools.logging :as log])
  (:use korma.core
        apoya.data.schema))

(defn site-exists? [domain]
  (let [site-count (-> (select sites
                               (aggregate (count :*) :cnt)
                               (where {:domain (name domain)}))
                       (first)
                       (:cnt))]
    (> site-count 0)))

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

(defn url-exists? [url]
  (let [url-count (-> (select restricted_urls
                              (aggregate (count :*) :cnt)
                              (where {:url url}))
                      (first)
                      (:cnt))]
    (> url-count 0)))

(defn create-url [data]
  (insert restricted_urls (values data)))


(ns apoya.data.error
  (:require [clojure.tools.logging :as log])
  (:use korma.core
        apoya.data.schema))

(defn find-error [error-id]
  (first (select error-events
                 (with errors)
                 (with error-sources)
                 (where {:event_sha1 error-id}))))

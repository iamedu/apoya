(ns fortress.util
  (:import [java.util UUID]))

(defn random-uuid-str []
  (.toString (UUID/randomUUID)))



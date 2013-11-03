(ns apoya.config
  (:require [nomad :refer [defconfig]]
            [clojure.java.io :as io]))

(declare apoya-config)

(defn load-config [config]
  (defconfig apoya-config (io/file config)))

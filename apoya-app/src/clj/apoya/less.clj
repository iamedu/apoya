(ns apoya.less
  (:require [apoya.fs :as fs])
  (:import [com.asual.lesscss LessOptions LessEngine]
           [java.io File]))

(def engine (LessEngine.))

(defn compile-less [input output]
  (let [in-file (File. input)
        out-file (File. output)]
    (.compile engine in-file out-file)))

(ns apoya.less
  (:require [apoya.fs :as fs])
  (:import [com.asual.lesscss LessOptions LessEngine]
           [java.io File]))

(defn jclouds-loader []
  (proxy [com.asual.lesscss.loader.ResourceLoader] []
    (exists [path]
      (fs/site-blob-exists? path))
    (load [path charset]
      (let [payload (-> (fs/get-site-blob path)
                        (.getPayload))
            text (slurp (.getInput payload))]
        (.release payload)
        text))))

(defn build-engine []
  (let [less-options (doto (LessOptions.)
                       (.setCompress true)
                       (.setOptimization (int 10)))]
    (LessEngine. less-options (jclouds-loader))))

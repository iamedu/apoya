(ns apoya.less
  (:require [apoya.fs :as fs]
            [apoya.config :as cfg]
            [clojure.tools.logging :as log]
            [clojure-watch.core :refer  [start-watch]])
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

(defn watch-folders [config]
  (let [engine (build-engine)]
    (doseq [{:keys [watch source target site]} config]
      (letfn [(compile-less []
                (binding [cfg/*current-site* site]
                  (fs/put-site-blob
                    target
                    (.compile engine (File. source)))
                  (log/info "Compiled into" target "for site" site)))]
        (start-watch [{:path watch
                       :event-types [:create :modify :delete]
                       :bootstrap (fn [path]
                                    (log/info "Starting to watch" path)
                                    (compile-less))
                       :callback (fn [event filename]
                                   (log/info "Less file modification" event filename site)
                                   (compile-less))
                       :options {:recursive true}}])))))


(ns apoya.services.apps
  (:require [apoya.config :as cfg]
            [apoya.resources.fs :as fs]
            [markdown.core :as md]
            [clojure.edn :as e]
            [org.jclouds.blobstore2 :as bstore]
            [clojure.tools.logging :as log]
            [clojure.string :as s]
            [clojure.java.io :as io])
  (:import [java.util.jar JarFile]))

(defn- read-entry [jar-file entry]
  (let [entry (.getEntry jar-file entry)]
    (with-open [is (.getInputStream jar-file entry)]
      (slurp is))))

(defn read-details [path]
  (let [apps-path (:apps-path (cfg/apoya-config))
        full-path (str apps-path "/" path)
        file (io/file full-path)
        jar-file (JarFile. file)
        changelog (-> (read-entry jar-file "classpath/APP_CHANGELOG.md") md/md-to-html-string)
        {:keys [commit version app-name]} (-> (read-entry jar-file "classpath/app-meta.edn") e/read-string)]
    {:commit commit :version version :changelog changelog :app-name app-name}))

(defn put-entry [jar-file entry]
  (let [parts (s/split (.getName entry) #"/")
        [container & parts] parts
        path (s/join "/" parts)]
    (if (.isDirectory entry)
      (fs/create-dir container path)
      (with-open [is (.getInputStream jar-file entry)]
        (log/info container path)
        (fs/put-any-blob container path is)))))

(defn copy-contents [path]
  (let [apps-path (:apps-path (cfg/apoya-config))
        full-path (str apps-path "/" path)
        file (-> full-path (io/file) (JarFile.))
        file-entries (enumeration-seq (.entries file))]
    (doseq [entry file-entries]
      (put-entry file entry))))

(defn deploy [path]
  (let [containers ["classpath" "sites"]]
    (doseq [c containers]
      (bstore/clear-container fs/sites-base c))
    (copy-contents path)
    true))

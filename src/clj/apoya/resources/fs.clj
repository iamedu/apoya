(ns apoya.resources.fs
  (:require [apoya.config :as cfg]
            [clojure.tools.logging :as log])
  (:use org.jclouds.blobstore2))

(declare sites-base)

(defn setup-blobstore [c]
  (let [{:keys [blob-type account encoded-key]} c]
    (def sites-base (blobstore blob-type account encoded-key))
    (if-not (container-exists? sites-base "sites")
      (create-container sites-base "sites"))))

(defn site-blob-exists? [path]
  (let [sites (conj #{cfg/*current-site*} :default)
        path (if (.startsWith path "/")
               path
               (str "/" path))]
    (some #(blob-exists? sites-base "sites" (str (name %) path)) sites)))

(defn get-site-blob [path]
  (let [path (if (.startsWith path "/")
               path
               (str "/" path))]
    (some #(get-blob sites-base "sites" (str (name %) path)) [cfg/*current-site* :default])))

(defn- convert-jclouds-file [f]
  (let [t (-> f (.getType) (.name))
        n (-> f (.getName))
        n (if (= t "BLOB")
            (last (clojure.string/split n #"/"))
            n)]
    {:name n :type t}))

(defn list-contents [folder]
  (let [path (if (.startsWith folder "/")
               folder
               (str "/" folder))
        base-path (name cfg/*current-site*)
        path (str base-path path)
        bs (container-seq sites-base "sites" path)]
    (map convert-jclouds-file bs)))

(defn put-site-blob [path source]
  (let [path (if (.startsWith path "/")
               path
               (str "/" path))
        path (str (name cfg/*current-site*) path)
        new-blob (blob path :payload (payload source))]
    (put-blob sites-base "sites" new-blob)))

(defn create-dir [container path]
  (create-directory sites-base container path))

(defn put-any-blob [container path source]
  (log/info container path)
  (let [new-blob (blob path :payload (payload source))]
    (put-blob sites-base container new-blob)))

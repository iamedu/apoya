(ns apoya.fs
  (:require [apoya.config :as cfg])
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


(ns sun.net.www.protocol.jclouds.connection
  (:require [org.jclouds.blobstore2 :as bs]
            [apoya.resources.fs :as fs])
  (:import (java.io FileNotFoundException))
  (:gen-class
    :name sun.net.www.protocol.jclouds.JCloudsConnection
    :extends java.net.URLConnection
    :main false))

(defn normalize-name [name]
  (apply str (drop-while #(= \/ %) name)))

(defn -connect [this]
  (let [file-name (normalize-name (-> this .getURL .getFile))]
    (if-not (bs/blob-exists? fs/sites-base "classpath" file-name)
      (throw (FileNotFoundException. file-name)))))

(defn -getInputStream [this]
  (let [file-name (normalize-name (-> this .getURL .getFile))]
    (if (bs/blob-exists? fs/sites-base "classpath" file-name)
      (bs/get-blob-stream fs/sites-base "classpath" file-name)
      (throw (FileNotFoundException. file-name)))))

(defn -getContent [this]
  (-getInputStream this))

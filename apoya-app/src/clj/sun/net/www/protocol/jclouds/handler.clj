(ns sun.net.www.protocol.jclouds.handler
  (:require [org.jclouds.blobstore2 :as bs])
  (:import sun.net.www.protocol.jclouds.JCloudsConnection)
  (:gen-class
    :name sun.net.www.protocol.jclouds.Handler
    :extends java.net.URLStreamHandler
    :main false))

(defn -openConnection [this url]
  (JCloudsConnection. url))


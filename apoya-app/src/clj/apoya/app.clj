(ns apoya.app
  (:use compojure.core
        fleet)
  (:require [apoya.config :as cfg]
            [apoya.fs :as fs]
            [apoya.i18n :as i18n]
            [compojure.route :as route]
            [pantomime.mime :refer [mime-type-of]]
            [noir.util.middleware :as middleware]
            [ring.util.response :as response]
            [ring.middleware.gzip :as gzip]
            [clojure.tools.logging :as log]
            [clojure.string :as s]))

(defn fleet-resource [{:keys [uri]}]
  (when (.endsWith uri ".html")
    (let [uri (s/replace uri #"\.html$" ".fleet")
          template (and (fs/site-blob-exists? uri)
                        (-> (fs/get-site-blob uri)
                            (.getPayload)
                            (.getInput)
                            (slurp)))]
      (when template
        (let [compile-fn (fleet [label] template {:escaping :xml})]
          {:body (.toString (compile-fn i18n/label))})))))

(defn jclouds-resource [{:keys [uri headers]}]
  (when (and (not (.endsWith uri "/"))
             (fs/site-blob-exists? uri))
    (let [mime-type (mime-type-of uri)
          if-none-match (get headers "if-none-match")
          res-blob (fs/get-site-blob uri)
          etag (-> res-blob
                   (.getMetadata)
                   (.getETag))
          body (-> res-blob
                   (.getPayload)
                   (.getInput))]
      (if (or (nil? etag) (not= etag if-none-match))
        (response/content-type {:body body
                                :headers {"etag" etag}
                                :status 200}
                               mime-type)
        {:status 304 :body "" :headers {"etag" etag}}))))

(defn handle-errors [handler]
  (fn [request]
    (try
      (handler request)
      (catch Exception e
        (log/fatal e "Cannot handle" request)
        {:status 500
         :body "Error"}))))

(defn site-chooser [handler]
  (fn [request]
    (binding [cfg/*current-site* (keyword (get-in request [:headers "host"]))]
      (handler request))))

(defroutes app-routes
  jclouds-resource
  fleet-resource
  (GET "/" [] (fleet-resource {:uri "/index.html"}))
  (route/not-found (fn [_]
                     (fleet-resource {:uri "/404.html"}))))

(def app (middleware/app-handler
           [app-routes]
           :middleware [handle-errors
                        site-chooser
                        gzip/wrap-gzip]
           :access-rules []
           :formats [:edn]))


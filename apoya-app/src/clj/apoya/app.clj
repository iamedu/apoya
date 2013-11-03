(ns apoya.app
  (:use compojure.core
        fleet)
  (:require [apoya.config :as cfg]
            [apoya.fs :as fs]
            [apoya.i18n :as i18n]
            [compojure.route :as route]
            [noir.util.middleware :as middleware]
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

(defn jclouds-resource [{:keys [uri]}]
  (when (and (not (.endsWith uri "/"))
             (fs/site-blob-exists? uri))
    {:body (-> (fs/get-site-blob uri)
               (.getPayload)
               (.getInput))}))

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
  (route/not-found "Not found"))

(def app (middleware/app-handler
           [app-routes]
           :middleware [handle-errors
                        site-chooser]
           :access-rules []
           :formats [:edn]))


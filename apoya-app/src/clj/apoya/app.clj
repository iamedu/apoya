(ns apoya.app
  (:use compojure.core
        fleet)
  (:require [apoya.config :as cfg]
            [apoya.i18n :as i18n]
            [apoya.errors :as errors]
            [apoya.resources.fs :as fs]
            [apoya.resources.optimize :as opt]
            [apoya.data.site :as site]
            [apoya.data.auth :as auth-data]
            [apoya.security.workflows :as workflows]
            [apoya.security.csrf :as csrf]
            [compojure.route :as route]
            [pantomime.mime :refer [mime-type-of]]
            [noir.util.middleware :as middleware]
            [ring.util.response :as response]
            [ring.middleware.gzip :as gzip]
            [ring.middleware.anti-forgery :as af]
            [cemerick.friend :as friend]
            [cemerick.friend.credentials :as creds]
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
        (errors/webapp-error request e)
        (let [accepts (get-in request [:headers "accept"])
              accepts-html (and accepts
                                (.contains accepts "text/html"))
              body (if accepts-html
                     (fleet-resource {:uri "/500.html"}))]
          (merge body {:status 500}))))))

(defn language-chooser [handler]
  (fn [request]
    (let [selected-language (i18n/find-supported-language request)]
      (binding [cfg/*language* selected-language]
        (handler request)))))

(defn site-chooser [handler]
  (fn [request]
    (let [prospected-site (keyword (get-in request [:headers "host"]))
          correct-site (if (site/site-exists? prospected-site)
                         prospected-site
                         :default)]
      (binding [cfg/*current-site* correct-site]
        (handler request)))))

(defroutes app-routes
  jclouds-resource
  fleet-resource
  (GET "/" [] (opt/links (fleet-resource {:uri "/index.html"})
                         ["css/bootstrap.css" :subresource]
                         ["bower_components/jquery/jquery.min.js" :subresource]
                         ["bower_components/jquery/jquery.min.map" :subresource]
                         ["bower_components/angular/angular.min.js" :subresource]
                         ["bower_components/store.js/store.min.js" :subresource]  
                         ["js/main.js" :subresource]))
  (POST "/api/v1/auth/login.edn" request
        (-> (response/response (pr-str (friend/identity request)))
            (response/content-type "application/edn; charset=utf-8")))
  (POST "/api/v1/auth/persona-login.edn" request
        (-> (response/response (pr-str (friend/identity request)))
            (response/content-type "application/edn; charset=utf-8")))
  (GET "/hola" [] (/ 1 0))
  (POST "/upload" request)
  (GET "/adios" [] (friend/authorize #{::admin}
                                     "Admin page"))
  (route/not-found (fn [_] (fleet-resource {:uri "/404.html"}))))

(def secured-routes
  (-> app-routes
      (friend/authenticate {:credential-fn (partial creds/bcrypt-credential-fn auth-data/find-user)
                            :workflows [(workflows/edn-workflow :login-uri "/api/v1/auth/login.edn")
                                        (workflows/persona-workflow :login-uri "/api/v1/auth/persona-login.edn")]})))

(def app (middleware/app-handler
           [secured-routes]
           :middleware [handle-errors
                        language-chooser
                        site-chooser
                        csrf/wrap-add-anti-forgery-cookie
                        af/wrap-anti-forgery 
                        opt/wrap-modern-ie
                        gzip/wrap-gzip]
           :access-rules []
           :formats [:edn]))


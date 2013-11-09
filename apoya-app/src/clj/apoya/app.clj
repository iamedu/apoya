(ns apoya.app
  (:use compojure.core
        fleet)
  (:require [apoya.config :as cfg]
            [apoya.i18n :as i18n]
            [apoya.errors :as errors]
            [apoya.response :as r]
            [apoya.resources.fs :as fs]
            [apoya.resources.optimize :as opt]
            [apoya.data.site :as site]
            [apoya.data.auth :as auth-data]
            [apoya.security.workflows :as workflows]
            [apoya.security.csrf :as csrf]
            [apoya.security.rules :as rules]
            [apoya.routes.auth :refer [auth-routes]]
            [compojure.route :as route]
            [pantomime.mime :refer [mime-type-of]]
            [noir.util.middleware :as middleware]
            [noir.util.route :refer [restricted]]
            [ring.util.response :as response]
            [ring.middleware.gzip :as gzip]
            [ring.middleware.anti-forgery :as af]
            [ring.middleware.head :as head]
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
          {:body (.toString (compile-fn i18n/label))
           :headers {"Content-Type" "text/html; charset=UTF-8"}})))))

(defn jclouds-resource [{:keys [uri headers]}]
  (when (and (not (.endsWith uri "/"))
             (fs/site-blob-exists? uri))
    (let [max-age (if (= uri "/js/main.js")
                    (* 30 60)
                    (* 5 24 60 60))
          mime-type (mime-type-of uri)
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
                                :headers {"etag" etag
                                          "cache-control" (str "max-age=" max-age)}
                                :status 200}
                               mime-type)
        {:status 304 :body "" :headers {"etag" etag
                                        "cache-control" (str "max-age=" max-age)}}))))

(defn handle-errors [handler]
  (fn [request]
    (try
      (handler request)
      (catch Exception e
        (let [error-id (errors/webapp-error request e)
              accepts (get-in request [:headers "accept"])
              accepts-html (and accepts
                                (.contains accepts "text/html"))
              body (if accepts-html
                     (fleet-resource {:uri "/500.html"})
                     (r/edn-response {:error-id error-id :error :internal-server-error}))]
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

(defn access-forbidden [request]
  (let [accept (get-in request [:headers "accept"])]
    (if (and accept (.contains accept "application/edn"))
      (assoc (r/edn-response {:error :forbidden :cause :no-permission}) :status 403)
      (assoc (fleet-resource {:uri "/403.html"}) :status 403))))

(defroutes app-routes
  jclouds-resource
  fleet-resource
  (GET "/" [] (opt/links (fleet-resource {:uri "/index.html"})
                         ["bower_components/jquery/jquery.min.js" :subresource]
                         ["bower_components/jquery/jquery.min.map" :subresource]
                         ["bower_components/nprogress/nprogress.js" :subresource]
                         ["bower_components/nprogress/nprogress.css" :subresource]
                         ["css/bootstrap.css" :subresource]
                         ["bower_components/store.js/store.min.js" :subresource]
                         ["bower_components/spin.js/spin.js" :subresource]
                         ["bower_components/select2/select2.min.js" :subresource]
                         ["bower_components/codemirror/lib/codemirror.js" :subresource]
                         ["bower_components/underscore/underscore-min.js" :subresource]
                         ["bower_components/Eventable/eventable.js" :subresource]
                         ["bower_components/sir-trevor-js/sir-trevor.min.js" :subresource]
                         ["bower_components/angular/angular.min.js" :subresource]
                         ["bower_components/angular-spinner/angular-spinner.min.js" :subresource]
                         ["bower_components/angular-ui-bootstrap-bower/ui-bootstrap-tpls.min.js" :subresource]
                         ["bower_components/angular-ui-select2/src/select2.js" :subresource]
                         ["js/main.js" :subresource]))
  (context "/api/public/v1/auth" [] auth-routes)
  (POST "/hola/:id.edn" [id hola]
        (r/edn-response {:id id :hola hola}))
  (POST "/upload" request)
  (GET "/adios" [] (friend/authorize #{::admin}
                                     "Admin page"))
  (route/not-found (fn [_] (fleet-resource {:uri "/404.html"}))))

(def secured-routes
  (-> app-routes
      (friend/authenticate {:credential-fn (partial creds/bcrypt-credential-fn
                                                    (partial auth-data/find-user :username))
                            :workflows [(workflows/edn-workflow :login-uri "/api/public/v1/auth/login.edn")
                                        (workflows/persona-workflow :login-uri "/api/public/v1/auth/persona-login.edn")]})))

(defn read-csrf-token [request]
  (get-in request [:params :__anti-forgery-token]))

(def invalid-csrf-response
  (assoc (r/edn-response {:error :forbidden :cause :invalid-csrf-token})
         :status 403))

(defn wrap-anti-forgery [handler]
  (af/wrap-anti-forgery handler
                        {:error-response invalid-csrf-response
                         :read-token read-csrf-token}))

(def app (middleware/app-handler
           [secured-routes]
           :middleware [handle-errors
                        language-chooser
                        site-chooser
                        csrf/wrap-add-anti-forgery-cookie
                        wrap-anti-forgery
                        opt/wrap-modern-ie
                        head/wrap-head
                        gzip/wrap-gzip]
           :access-rules [{:rule #'rules/check-access
                           :on-fail #'access-forbidden}]
           :formats [:edn]))


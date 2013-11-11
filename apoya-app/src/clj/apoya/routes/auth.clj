(ns apoya.routes.auth
  (:use compojure.core)
  (:require [cemerick.friend :as friend]
            [apoya.response :as r]))

(defn- identity-response [request]
  (let [session (:session request)]
    (assoc (r/edn-response (friend/identity request))
           :session session)))

(defroutes auth-routes
  (POST "/login.edn" request (identity-response request))
  (POST "/persona-login.edn" request (identity-response request))
  (POST "/identity.edn" request (identity-response request))
  (friend/logout (ANY "/logout.edn" request
                      (assoc (r/edn-response nil)
                             :session nil))))

(defroutes private-auth-routes)

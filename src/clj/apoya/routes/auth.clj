(ns apoya.routes.auth
  (:use compojure.core)
  (:require [cemerick.friend :as friend]
            [apoya.security.permission :as perm]
            [apoya.response :as r]
            [clojure.tools.logging :as log]))

(defn- identity-response [request]
  (let [session (:session request)]
    (assoc (r/edn-response (friend/identity request))
           :session session)))

(defroutes auth-routes
  (POST "/login.edn" request (identity-response request))
  (POST "/persona-login.edn" request (identity-response request))
  (POST "/identity.edn" request (identity-response request))
  (POST "/has-permissions.edn" {session :session params :body-params}
        (let [{permissions :permissions} params
              permissions (if (instance? String permissions)
                            [permissions]
                            permissions)
              username (get-in session [:cemerick.friend/identity :current])
              has-permissions (map #(perm/has-permission? % :username username) permissions)]
          (r/edn-response (zipmap permissions has-permissions)))) 
  (friend/logout (ANY "/logout.edn" request
                      (assoc (r/edn-response nil)
                             :session nil))))

(defroutes private-auth-routes)

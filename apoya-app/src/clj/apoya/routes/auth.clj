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

(defn merge-role [response session role]
  (let [{current :current} (:cemerick.friend/identity session)
        session (update-in session [:cemerick.friend/identity :authentications current]
                           assoc :current-role role)]
    (assoc response :session session)))

(defroutes private-auth-routes
  (POST "/select-role.edn" request
        (let [{:keys [role_code description]} (:params request)
              id (friend/current-authentication request)
              roles (set (map :role_code (:roles id)))
              session (:session request)
              role {:role_code role_code :description description}]
          (if (roles role_code)
            (merge-role (r/edn-response {:role role}) session role) 
            (assoc (r/edn-response {:cause :role-not-available})
                   :status 403)))))


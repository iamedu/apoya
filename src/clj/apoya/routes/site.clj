(ns apoya.routes.site
  (:use compojure.core)
  (:require [apoya.response :as r]
            [apoya.security.permission :as permission]
            [apoya.data.error :as err]
            [cemerick.friend :as friend]
            [clojure.tools.logging :as log]))

(defn restore-user [session]
  (let [{:keys [current authentications]} (:cemerick.friend/identity session)
        original-user (-> authentications (get current) :original-user)
        authentications (dissoc authentications current)
        ids {:current original-user :authentications authentications}]
    (assoc session :cemerick.friend/identity ids)))

(defroutes site-routes
  (POST "/impersonate.edn" {session :session :as request}
        (let [{:keys [original-user identity]} (friend/current-authentication request)]
          (if-not (nil? original-user)
            (assoc (r/edn-response true) :session session)
            (r/edn-response false))))
  (POST "/end-impersonation.edn" {session :session :as request}
        (let [{:keys [original-user identity]} (friend/current-authentication request)]
          (if-not (nil? original-user)
            (assoc (r/edn-response true) :session (restore-user session))
            (r/edn-response false))))
  (POST "/list-context-changes.edn" {session :session :as request}
        (let [{:keys [original-user identity]} (friend/current-authentication request)
              context-changes (if original-user
                                {:impersonate {:original-user original-user :current identity}}
                                {})]
          (r/edn-response context-changes))))

(defroutes private-site-routes
  (POST "/can-impersonate.edn" {params :body-params :as request}
        (let [{:keys [user]} params
              id (get (friend/current-authentication request) :identity)]
          (r/edn-response (and (not= id user)
                               (permission/can-impersonate? user :username id))))))

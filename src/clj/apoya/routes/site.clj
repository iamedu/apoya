(ns apoya.routes.site
  (:use compojure.core)
  (:require [apoya.response :as r]
            [apoya.config :as cfg]
            [apoya.security.permission :as permission]
            [apoya.data.error :as err]
            [apoya.data.site :as site]
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
  (POST "/end-change-site.edn" {session :session}
        (assoc (r/edn-response true)
               :session (dissoc session :changed-site)))
  (POST "/list-context-changes.edn" {session :session :as request}
        (let [{:keys [original-user identity]} (friend/current-authentication request)
              {:keys [changed-site]} session
              context-changes (if original-user
                                {:impersonate {:original-user original-user :current identity}}
                                {})
              context-changes (if changed-site
                                (assoc context-changes :site {:current changed-site})
                                context-changes)]
          (r/edn-response context-changes))))

(defroutes private-site-routes
  (POST "/list-sites.edn" request
        (let [sites (site/list-sites)
              current-site (name cfg/*current-site*)
              sites (filter #(not= current-site (:domain %)) sites)]
          (r/edn-response sites)))
  (POST "/change-site.edn" {:keys [body-params session]}
        (let [{:keys [domain]} body-params
              prospected-site (keyword domain)
              correct-site (if (site/site-exists? prospected-site)
                             prospected-site
                             :default)]
          (if (= prospected-site correct-site)
            (assoc (r/edn-response true) :session (assoc session :changed-site correct-site))
            (r/edn-response false))))
  (POST "/can-impersonate.edn" {params :body-params :as request}
        (let [{:keys [user]} params
              id (get (friend/current-authentication request) :identity)]
          (r/edn-response (and (not= id user)
                               (permission/can-impersonate? user :username id))))))

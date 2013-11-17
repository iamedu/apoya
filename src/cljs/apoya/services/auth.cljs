(ns apoya.services.auth
  (:require-macros [cljs.core.async.macros :refer [go]]) 
  (:require [apoya.remote.request :as r]
            [apoya.topics :as t]
            [apoya.util.log :as log]
            [cljs.core.async :refer [<!]]
            [goog.async.Delay]))

(declare check-user)
(def user (atom nil))
(def role (atom nil))
(def started (atom false))

(defn select-role [role]
  (r/edn [:post "/api/v1/auth/select-role.edn"]
         :content (js->clj role)))

(defn find-current-user []
  (r/edn [:post "/api/public/v1/auth/identity.edn"]))

(defn login [user]
  (r/edn [:post "/api/public/v1/auth/login.edn"]
         :content user))

(defn has-permissions [permissions]
  (r/edn [:post "/api/public/v1/auth/has-permissions.edn"]
         :content {:permissions permissions}))

(defn can-impersonate? [user]
  (r/edn [:post "/api/v1/site/can-impersonate.edn"]
         :content {:user user}))

(defn impersonate [username]
  (r/edn [:post "/api/public/v1/site/impersonate.edn"]
          :content {:username username}))

(defn end-impersonation []
  (r/edn [:post "/api/public/v1/site/end-impersonation.edn"]))

(defn logout []
  (r/edn [:post "/api/public/v1/auth/logout.edn"]))

(defn check-user [& [callback]]
  (go
    (let [{:keys [outcome body]} (<! (find-current-user))]
      (reset! started true)
      (t/publish :ready true) 
      (when body
        (t/publish :identity body))
      (when (and (nil? body)
                 (not (nil? @user)))
        (t/publish :logout true))
      (if callback (callback))
      (.start (goog.async.Delay. check-user (* 60 1000))))))

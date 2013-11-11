(ns apoya.services.auth
  (:require-macros [cljs.core.async.macros :refer [go]]) 
  (:require [apoya.remote.request :as r]
            [apoya.topics :as t]
            [apoya.util.log :as log]
            [cljs.core.async :refer [<!]]
            [goog.async.Delay]))

(declare check-user)
(def user (atom nil))

(defn find-current-user []
  (r/edn [:post "/api/public/v1/auth/identity.edn"]))

(defn login [user]
  (r/edn [:post "/api/public/v1/auth/login.edn"]
         :content user))

(defn logout []
  (r/edn [:post "/api/public/v1/auth/logout.edn"]))

(defn check-user []
  (go
    (let [{:keys [outcome body]} (<! (find-current-user))]
      (t/publish :ready true) 
      (when body
        (t/publish :identity body))
      (when (and (nil? body)
                 (not (nil? @user)))
        (t/publish :logout true))
      (.done js/NProgress)
      (.start (goog.async.Delay. check-user (* 60 1000))))))

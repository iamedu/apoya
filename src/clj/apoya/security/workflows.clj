(ns apoya.security.workflows
  (:require [apoya.security.persona :as persona]
            [apoya.data.site :as site]
            [apoya.data.auth :as auth]
            [cemerick.friend.workflows :as workflows]
            [cemerick.friend.util :refer (gets)]
            [cemerick.friend :as friend]
            [ring.util.response :as res]
            [ring.util.request :as req]
            [clojure.tools.logging :as log]))

(defn persona-credential-fn  [{:keys [email] :as credential-map}]
  (when (persona/valid? credential-map)
    (if-let [user (auth/find-user :email email)]
      (merge {:identity (:username user)}
             credential-map    
             (dissoc user :password)))))

(defn persona-workflow [& {:keys [login-uri]}]
  (fn [request]
    (when (and (= (:uri request) login-uri)
               (=  (:request-method request) :post))
      (let [{:keys [scheme headers]} request
            host (get headers "host")
            possible-audience (str (name scheme) "://" host)
            audience (if (site/site-exists? host)
                       possible-audience)]
        (-> request
            :params
            :assertion
            (persona/verify-assertion possible-audience)
            persona-credential-fn
            (workflows/make-auth {::friend/workflow :edn-request
                                  ::friend/redirect-on-auth? false}))))))

(defn edn-workflow [& {:keys [login-uri] :as edn-config}]
  (fn [{:keys [request-method params body-params] :as request}]
    (when (and (= (gets :login-uri edn-config (::friend/auth-config request)) (req/path-info request))
               (= :post request-method))
      (let [creds {:username (get body-params :username "")
                   :password (:password params)}
            {:keys [username password]} creds
            credential-fn (gets :credential-fn edn-config (::friend/auth-config request))]
        (if-let [user-record (and username password
                                  (credential-fn (with-meta creds {::friend/workflow :edn-request})))]
          (workflows/make-auth user-record {::friend/workflow :edn-request
                                            ::friend/redirect-on-auth? false}))))))



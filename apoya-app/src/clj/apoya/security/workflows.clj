(ns apoya.security.workflows
  (:require [cemerick.friend.workflows :as workflows]
            [cemerick.friend.util :refer (gets)]
            [cemerick.friend :as friend]
            [ring.util.response :as res]
            [ring.util.request :as req]
            [clojure.tools.logging :as log]))

(defn edn-request [& {:keys [login-uri] :as edn-config}]
  (fn [{:keys [request-method params form-params] :as request}]
    (when (and (= (gets :login-uri edn-config (::friend/auth-config request)) (req/path-info request))
               (= :post request-method))
      (let [creds {:username (get form-params "username" "")
                   :password (:password params)}
            {:keys [username password]} creds
            credential-fn (gets :credential-fn edn-config (::friend/auth-config request))]
        (if-let [user-record (and username password
                                  (credential-fn (with-meta creds {::friend/workflow :edn-request})))]
          (workflows/make-auth user-record {::friend/workflow :edn-request
                                              ::friend/redirect-on-auth? false}))))))


